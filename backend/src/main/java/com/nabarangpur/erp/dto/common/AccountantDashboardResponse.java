package com.nabarangpur.erp.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter @Builder @AllArgsConstructor
public class AccountantDashboardResponse {
    private BigDecimal todaysCollection;
    private long pendingDueInvoices;
    private long receiptsGeneratedTotal;
}
