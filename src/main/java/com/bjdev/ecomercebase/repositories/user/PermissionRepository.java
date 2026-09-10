package com.bjdev.ecomercebase.repositories.user;

import com.bjdev.ecomercebase.models.user.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
}
