package com.nabarangpur.erp.dto.faculty;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreateFacultyProfileRequest {
    @NotNull private Long userId;
    private Long departmentId;
    private String employeeCode;
    private String designation;
}
