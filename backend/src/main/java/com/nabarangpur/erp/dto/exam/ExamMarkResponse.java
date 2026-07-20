package com.nabarangpur.erp.dto.exam;

import com.nabarangpur.erp.entity.ExamMark;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class ExamMarkResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private String componentCode;
    private BigDecimal marksObtained;
    private BigDecimal maxMarks;

    public static ExamMarkResponse from(ExamMark m) {
        return ExamMarkResponse.builder()
                .id(m.getId())
                .studentId(m.getStudent().getId())
                .studentName(m.getStudent().getFullName())
                .componentCode(m.getComponent().getCode())
                .marksObtained(m.getMarksObtained())
                .maxMarks(m.getMaxMarks())
                .build();
    }
}
