package com.bjdev.ecomercebase.services.impl.user;

import com.bjdev.ecomercebase.dto.response.UserResponse;
import com.bjdev.ecomercebase.exception.AuthException;
import com.bjdev.ecomercebase.mappers.UserMapper;
import com.bjdev.ecomercebase.models.user.User;
import com.bjdev.ecomercebase.repositories.user.UserRepository;
import com.bjdev.ecomercebase.security.CurrentUserProvider;
import com.bjdev.ecomercebase.security.jwt.TokenBlackListService;
import com.bjdev.ecomercebase.services.impl.audit.AuditService;
import com.bjdev.ecomercebase.services.interfaces.UserService;
import lombok.RequiredArgsConstructor;
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
    private final CurrentUserProvider currentUserProvider;

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
        User user = currentUserProvider.getCurrentUser();
        user.setIsActive(false);
        userRepository.save(user);
        tokenBlackListService.invalidateAllUserTokens(user.getEmail());
        auditService.record(user, "USER_SELF_DISABLED", "users", user.getId(), null, null, null);
    }
}
