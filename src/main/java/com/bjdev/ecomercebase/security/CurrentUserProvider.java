package com.bjdev.ecomercebase.security;

import com.bjdev.ecomercebase.exception.AuthException;
import com.bjdev.ecomercebase.models.user.User;
import com.bjdev.ecomercebase.repositories.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Resolves the authenticated User from the security context — the one piece of logic every service that needs "who did this" used to duplicate. */
@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmailWithRolesAndPermisos(email)
                .orElseThrow(AuthException::invalidCredentials);
    }

    /** Same as getCurrentUser, but null instead of throwing — for call sites (e.g. logout) where an expired/anonymous session is still a valid case to audit. */
    public User getCurrentUserOrNull() {
        try {
            return getCurrentUser();
        } catch (Exception e) {
            return null;
        }
    }
}
