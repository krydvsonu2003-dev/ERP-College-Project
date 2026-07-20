package com.nabarangpur.erp.dto.common;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class CreateCourseRequest {
    @NotNull private Long departmentId;
    @NotBlank private String name;
    @NotBlank private String code;
    @NotNull private BigDecimal durationYears;
    @NotNull private Integer totalSemesters;
}
