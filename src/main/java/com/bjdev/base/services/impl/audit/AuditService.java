package com.bjdev.base.services.impl.audit;

import com.bjdev.base.models.user.AuditLog;
import com.bjdev.base.models.user.User;
import com.bjdev.base.repositories.user.AuditLogRepository;
import com.bjdev.base.utils.HttpRequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void record(User user, String action, String tableName, Long recordId,
                        Map<String, Object> oldValues, Map<String, Object> newValues,
                        HttpServletRequest request) {

        AuditLog.AuditLogBuilder builder = AuditLog.builder()
                .user(user)
                .action(action)
                .tableName(tableName)
                .recordId(recordId);

        if (oldValues != null) builder.oldValues(oldValues);
        if (newValues != null) builder.newValues(newValues);

        if (request != null) {
            builder.ipAddress(HttpRequestUtils.getClientIp(request));
            builder.userAgent(HttpRequestUtils.getUserAgent(request));
        }

        auditLogRepository.save(builder.build());
    }
}
