package com.bjdev.ecomercebase.repositories.user;

import com.bjdev.ecomercebase.models.user.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(String name);
}
