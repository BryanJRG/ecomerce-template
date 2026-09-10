package com.bjdev.ecomercebase.services.impl.client;

import com.bjdev.ecomercebase.models.client.Client;
import com.bjdev.ecomercebase.models.user.User;
import com.bjdev.ecomercebase.repositories.client.ClientRepository;
import com.bjdev.ecomercebase.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Resolves the commerce {@link Client} for the currently authenticated {@link User} — created
 * lazily on first use, per Client's class doc. Guest checkout (no User/JWT at all) is NOT wired
 * yet: every cart/checkout/order endpoint today requires a logged-in session. Adding it later means
 * introducing a separate guest-identity mechanism (e.g. a signed cart cookie) and a "claim this
 * cart on login" step — deliberately out of scope here rather than guessed at.
 */
@Component
@RequiredArgsConstructor
public class ClientResolver {

    private final ClientRepository clientRepository;
    private final CurrentUserProvider currentUserProvider;

    /** Read-only lookup — does NOT create a Client, so listing "my orders"/"my cart" for someone who never checked out just sees an empty result instead of a row being created. */
    public Optional<Client> findCurrentClient() {
        User user = currentUserProvider.getCurrentUser();
        return clientRepository.findByUserId(user.getId());
    }

    /** For write paths (add to cart, checkout) that need a Client row to attach to. */
    @Transactional
    public Client getOrCreateCurrentClient() {
        return findCurrentClient().orElseGet(this::createForCurrentUser);
    }

    private Client createForCurrentUser() {
        User user = currentUserProvider.getCurrentUser();
        return clientRepository.save(Client.builder()
                .user(user)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build());
    }
}
