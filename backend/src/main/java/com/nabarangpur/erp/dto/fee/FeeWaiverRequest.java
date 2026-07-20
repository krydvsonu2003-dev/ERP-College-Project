package com.nabarangpur.erp.dto.fee;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class FeeWaiverRequest {
    @NotNull private Long studentFeeAssignmentId;
    @NotNull @DecimalMin(value = "0.01")
    private BigDecimal waiverAmount;
    @NotBlank private String reason;
}
