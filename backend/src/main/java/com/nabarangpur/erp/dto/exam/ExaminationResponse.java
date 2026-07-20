package com.nabarangpur.erp.dto.exam;

import com.nabarangpur.erp.entity.Examination;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class ExaminationResponse {
    private Long id;
    private String name;
    private Long courseId;
    private String courseName;
    private Integer semester;
    private String examType;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;

    public static ExaminationResponse from(Examination e) {
        return ExaminationResponse.builder()
                .id(e.getId()).name(e.getName())
                .courseId(e.getCourse().getId()).courseName(e.getCourse().getName())
                .semester(e.getSemester()).examType(e.getExamType().name())
                .status(e.getStatus().name())
                .startDate(e.getStartDate()).endDate(e.getEndDate())
                .build();
    }
}
