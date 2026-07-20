package com.nabarangpur.erp.dto.admission;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RejectAdmissionRequest {
    @NotBlank(message = "Rejection remarks are required")
    private String remarks;
}
