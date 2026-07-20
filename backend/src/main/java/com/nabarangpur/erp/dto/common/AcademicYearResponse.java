package com.nabarangpur.erp.dto.common;

import com.nabarangpur.erp.entity.AcademicYear;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter @Builder @AllArgsConstructor
public class AcademicYearResponse {
    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean current;

    public static AcademicYearResponse from(AcademicYear a) {
        return AcademicYearResponse.builder()
                .id(a.getId()).name(a.getName())
                .startDate(a.getStartDate()).endDate(a.getEndDate())
                .current(a.isCurrent())
                .build();
    }
}
