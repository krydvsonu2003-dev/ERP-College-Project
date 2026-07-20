package com.nabarangpur.erp.dto.fee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class PaymentReceiptResponse {
    private Long paymentId;
    private String receiptNumber;
    private Long studentId;
    private String studentName;
    private String studentCode;
    private BigDecimal amount;
    private String paymentMode;
    private String paymentReference;
    private BigDecimal invoiceDueAmountAfter;
    private String invoiceStatusAfter;
    private Instant paidAt;
    private String receivedByName;
}
