package com.nabarangpur.erp.controller;

import com.nabarangpur.erp.dto.common.ApiResponse;
import com.nabarangpur.erp.dto.common.PageResponse;
import com.nabarangpur.erp.entity.AuditLog;
import com.nabarangpur.erp.repository.AuditLogRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "Functional Spec 9 - Audit & Compliance")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('AUDIT_READ')")
    public ApiResponse<PageResponse<AuditLog>> list(@RequestParam(required = false) String entityName,
                                                      Pageable pageable) {
        var page = (entityName == null || entityName.isBlank())
                ? auditLogRepository.findAllByOrderByCreatedAtDesc(pageable)
                : auditLogRepository.findByEntityNameOrderByCreatedAtDesc(entityName, pageable);
        return ApiResponse.ok(PageResponse.from(page));
    }
}
