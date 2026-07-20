package com.nabarangpur.erp.dto.common;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreateFeeHeadRequest {
    @NotBlank private String name;
    @NotBlank private String code;
    private String description;
}
