package com.nabarangpur.erp.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter @Builder @AllArgsConstructor
public class StudentDashboardResponse {
    private double attendancePercentage;
    private long publishedResultCount;
    private BigDecimal totalDueAmount;
    private int profileCompletenessPercent;
}
