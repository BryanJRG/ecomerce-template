package com.bjdev.ecomercebase.repositories.user;

import com.bjdev.ecomercebase.models.user.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {



}
