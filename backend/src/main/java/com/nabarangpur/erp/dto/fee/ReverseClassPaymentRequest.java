package com.nabarangpur.erp.dto.fee;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReverseClassPaymentRequest {
    @NotBlank(message = "A reason is required to reverse a posted payment")
    private String reason;
}
