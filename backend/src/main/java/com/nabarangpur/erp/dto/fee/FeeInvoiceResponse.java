package com.nabarangpur.erp.dto.fee;

import com.nabarangpur.erp.entity.FeeInvoice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class FeeInvoiceResponse {
    private Long id;
    private String invoiceNumber;
    private Long studentId;
    private String studentName;
    private String studentCode;
    private Integer semester;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal dueAmount;
    private String status;
    private LocalDate dueDate;

    public static FeeInvoiceResponse from(FeeInvoice inv) {
        return FeeInvoiceResponse.builder()
                .id(inv.getId())
                .invoiceNumber(inv.getInvoiceNumber())
                .studentId(inv.getStudent().getId())
                .studentName(inv.getStudent().getFullName())
                .studentCode(inv.getStudent().getStudentCode())
                .semester(inv.getSemester())
                .totalAmount(inv.getTotalAmount())
                .paidAmount(inv.getPaidAmount())
                .dueAmount(inv.getDueAmount())
                .status(inv.getStatus().name())
                .dueDate(inv.getDueDate())
                .build();
    }
}
