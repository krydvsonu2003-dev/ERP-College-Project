package com.nabarangpur.erp.dto.exam;

import com.nabarangpur.erp.entity.SemesterResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class SemesterResultResponse {
    private Long id;
    private Long studentId;
    private Integer semester;
    private BigDecimal sgpa;
    private BigDecimal cgpa;
    private BigDecimal totalCredits;
    private String status;

    public static SemesterResultResponse from(SemesterResult r) {
        return SemesterResultResponse.builder()
                .id(r.getId())
                .studentId(r.getStudent().getId())
                .semester(r.getSemester())
                .sgpa(r.getSgpa())
                .cgpa(r.getCgpa())
                .totalCredits(r.getTotalCredits())
                .status(r.getStatus().name())
                .build();
    }
}
