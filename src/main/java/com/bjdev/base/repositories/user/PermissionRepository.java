package com.bjdev.base.repositories.user;

import com.bjdev.base.models.user.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
}
