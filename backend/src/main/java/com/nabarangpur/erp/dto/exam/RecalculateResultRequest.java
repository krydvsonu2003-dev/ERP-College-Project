package com.nabarangpur.erp.dto.exam;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecalculateResultRequest {
    @NotBlank(message = "A reason is required to recalculate a published result")
    private String reason;
}
