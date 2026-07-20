package com.nabarangpur.erp.dto.fee;

import com.nabarangpur.erp.entity.PaymentMode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RecordPaymentRequest {
    @NotNull private Long invoiceId;
    @NotNull @DecimalMin(value = "0.01", message = "Payment amount must be greater than zero")
    private BigDecimal amount;
    @NotNull private PaymentMode paymentMode;
    private String paymentReference;
}
