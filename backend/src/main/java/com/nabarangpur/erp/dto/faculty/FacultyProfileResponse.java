package com.nabarangpur.erp.dto.faculty;

import com.nabarangpur.erp.entity.FacultyProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder @AllArgsConstructor
public class FacultyProfileResponse {
    private Long id;
    private Long userId;
    private String fullName;
    private String employeeCode;
    private String designation;
    private Long departmentId;

    public static FacultyProfileResponse from(FacultyProfile f) {
        return FacultyProfileResponse.builder()
                .id(f.getId())
                .userId(f.getUser().getId())
                .fullName(f.getUser().getFullName())
                .employeeCode(f.getEmployeeCode())
                .designation(f.getDesignation())
                .departmentId(f.getDepartment() != null ? f.getDepartment().getId() : null)
                .build();
    }
}
