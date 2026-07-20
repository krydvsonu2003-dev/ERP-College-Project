package com.nabarangpur.erp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nabarangpur.erp.entity.AuditLog;
import com.nabarangpur.erp.repository.AuditLogRepository;
import com.nabarangpur.erp.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.stream.Collectors;

/**
 * Central place for writing audit_logs rows. Every module's "critical
 * action" (per spec section 9: user create/status change, admission
 * approve/reject, attendance edits, marks updates, result publication,
 * fee collection/reversal) should call record(...) here.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void record(String action, String entityName, Object entityId, Object before, Object after) {
        try {
            CustomUserDetails principal = currentUser();
            AuditLog.AuditLogBuilder builder = AuditLog.builder()
                    .action(action)
                    .entityName(entityName)
                    .entityId(entityId == null ? null : String.valueOf(entityId))
                    .beforeValue(before == null ? null : safeJson(before))
                    .afterValue(after == null ? null : safeJson(after))
                    .ipAddress(currentIp());

            if (principal != null) {
                builder.userId(principal.getId())
                        .username(principal.getUsername())
                        .roleNames(String.join(",", principal.getRoleNames()));
            }
            auditLogRepository.save(builder.build());
        } catch (Exception ex) {
            // Auditing must never break the main business transaction.
            log.error("Failed to write audit log for action={} entity={}", action, entityName, ex);
        }
    }

    private String safeJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            return String.valueOf(o);
        }
    }

    private CustomUserDetails currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails cud) {
            return cud;
        }
        return null;
    }

    private String currentIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return null;
            HttpServletRequest request = attrs.getRequest();
            String xff = request.getHeader("X-Forwarded-For");
            return xff != null ? xff.split(",")[0].trim() : request.getRemoteAddr();
        } catch (Exception e) {
            return null;
        }
    }
}
