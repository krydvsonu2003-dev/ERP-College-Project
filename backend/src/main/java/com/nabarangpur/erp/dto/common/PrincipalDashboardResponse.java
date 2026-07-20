package com.nabarangpur.erp.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Map;

@Getter @Builder @AllArgsConstructor
public class PrincipalDashboardResponse {
    private long totalStudents;
    private long activeStudents;
    private long pendingAdmissions;
    private long pendingFeeInvoices;
    private BigDecimal totalFeeCollected;
    private BigDecimal totalFeeDue;
    private long publishedResultCards;
    private long draftResultCards;
    private Map<String, Long> studentsByDepartment;
}
