package com.nabarangpur.erp.dto.common;

import com.nabarangpur.erp.entity.ClassSection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder @AllArgsConstructor
public class ClassSectionResponse {
    private Long id;
    private Long courseId;
    private String courseName;
    private Long academicYearId;
    private Integer semester;
    private String sectionName;

    public static ClassSectionResponse from(ClassSection cs) {
        return ClassSectionResponse.builder()
                .id(cs.getId())
                .courseId(cs.getCourse().getId())
                .courseName(cs.getCourse().getName())
                .academicYearId(cs.getAcademicYear().getId())
                .semester(cs.getSemester())
                .sectionName(cs.getSectionName())
                .build();
    }
}
