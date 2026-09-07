package com.bjdev.base.services.impl.user;

import com.bjdev.base.dto.response.UserResponse;
import com.bjdev.base.exception.AuthException;
import com.bjdev.base.mappers.UserMapper;
import com.bjdev.base.models.user.User;
import com.bjdev.base.repositories.user.UserRepository;
import com.bjdev.base.security.jwt.TokenBlackListService;
import com.bjdev.base.services.impl.audit.AuditService;
import com.bjdev.base.services.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final TokenBlackListService tokenBlackListService;
    private final AuditService auditService;

    @Override
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream().map(userMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public void disableUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(AuthException::userNotFound);
        user.setIsActive(false);
        userRepository.save(user);
        tokenBlackListService.invalidateAllUserTokens(user.getEmail());
        auditService.record(user, "USER_DISABLED", "users", user.getId(), null, null, null);
    }

    @Override
    @Transactional
    public void enableUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(AuthException::userNotFound);
        user.setIsActive(true);
        userRepository.save(user);
        auditService.record(user, "USER_ENABLED", "users", user.getId(), null, null, null);
    }

    @Override
    @Transactional
    public void disableSelf() {
        User user = getAuthenticatedUser();
        user.setIsActive(false);
        userRepository.save(user);
        tokenBlackListService.invalidateAllUserTokens(user.getEmail());
        auditService.record(user, "USER_SELF_DISABLED", "users", user.getId(), null, null, null);
    }

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmailWithRolesAndPermisos(email)
                .orElseThrow(AuthException::invalidCredentials);
    }
}
