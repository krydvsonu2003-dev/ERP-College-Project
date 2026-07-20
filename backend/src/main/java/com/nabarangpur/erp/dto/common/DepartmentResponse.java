package com.nabarangpur.erp.dto.common;

import com.nabarangpur.erp.entity.Department;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder @AllArgsConstructor
public class DepartmentResponse {
    private Long id;
    private String name;
    private String code;

    public static DepartmentResponse from(Department d) {
        return DepartmentResponse.builder().id(d.getId()).name(d.getName()).code(d.getCode()).build();
    }
}
