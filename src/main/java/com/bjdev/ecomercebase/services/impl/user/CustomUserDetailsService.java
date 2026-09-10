package com.bjdev.ecomercebase.services.impl.user;

import com.bjdev.ecomercebase.models.user.User;
import com.bjdev.ecomercebase.repositories.user.UserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    private final String USER_NOT_FOUND_EXCEPTION_MESSAGE = "Usuario no encontrado con el email: ";


    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmailWithRolesAndPermisos(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        USER_NOT_FOUND_EXCEPTION_MESSAGE + email
                ));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash() == null ? "" : user.getPasswordHash())
                .authorities(buildAuthorities(user))
                .disabled(!user.getIsActive())
                .accountLocked(isLocked(user))
                .build();
    }

    private Set<GrantedAuthority> buildAuthorities(User user) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        user.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
            role.getPermissions().forEach(permission ->
                    authorities.add(new SimpleGrantedAuthority(permission.getName())));
        });

        return authorities;
    }

    private boolean isLocked(User usuario) {
        return usuario.getLockedUntil() != null && usuario.getLockedUntil().isAfter(LocalDateTime.now());
    }
}
