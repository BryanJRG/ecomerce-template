package com.bjdev.ecomercebase.repositories.auth;

import com.bjdev.ecomercebase.models.auth.AuthProvider;
import com.bjdev.ecomercebase.models.auth.LoginMethod;
import com.bjdev.ecomercebase.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginMethodRepository extends JpaRepository<LoginMethod, Integer> {
    boolean existsByUserAndProvider(User user, AuthProvider provider);
}
