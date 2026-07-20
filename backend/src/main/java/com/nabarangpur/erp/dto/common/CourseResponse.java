package com.nabarangpur.erp.dto.common;

import com.nabarangpur.erp.entity.Course;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter @Builder @AllArgsConstructor
public class CourseResponse {
    private Long id;
    private String name;
    private String code;
    private Long departmentId;
    private String departmentName;
    private BigDecimal durationYears;
    private Integer totalSemesters;

    public static CourseResponse from(Course c) {
        return CourseResponse.builder()
                .id(c.getId()).name(c.getName()).code(c.getCode())
                .departmentId(c.getDepartment().getId())
                .departmentName(c.getDepartment().getName())
                .durationYears(c.getDurationYears())
                .totalSemesters(c.getTotalSemesters())
                .build();
    }
}
