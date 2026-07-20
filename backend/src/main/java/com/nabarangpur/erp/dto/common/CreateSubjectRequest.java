package com.nabarangpur.erp.dto.common;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class CreateSubjectRequest {
    @NotNull private Long courseId;
    @NotNull private Integer semester;
    @NotBlank private String name;
    @NotBlank private String code;
    private BigDecimal credits;
}
