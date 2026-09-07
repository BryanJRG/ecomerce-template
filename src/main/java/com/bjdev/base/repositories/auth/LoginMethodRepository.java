package com.bjdev.base.repositories.auth;

import com.bjdev.base.models.auth.AuthProvider;
import com.bjdev.base.models.auth.LoginMethod;
import com.bjdev.base.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginMethodRepository extends JpaRepository<LoginMethod, Integer> {
    boolean existsByUserAndProvider(User user, AuthProvider provider);
}
