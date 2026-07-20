package com.nabarangpur.erp.dto.exam;

import com.nabarangpur.erp.entity.ResultCard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class ResultCardResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long subjectId;
    private String subjectName;
    private BigDecimal totalMarksObtained;
    private BigDecimal totalMaxMarks;
    private BigDecimal percentage;
    private String gradeLetter;
    private BigDecimal gradePoint;
    private BigDecimal credits;
    private String status;

    public static ResultCardResponse from(ResultCard r) {
        return ResultCardResponse.builder()
                .id(r.getId())
                .studentId(r.getStudent().getId())
                .studentName(r.getStudent().getFullName())
                .subjectId(r.getSubject().getId())
                .subjectName(r.getSubject().getName())
                .totalMarksObtained(r.getTotalMarksObtained())
                .totalMaxMarks(r.getTotalMaxMarks())
                .percentage(r.getPercentage())
                .gradeLetter(r.getGrade() != null ? r.getGrade().getGradeLetter() : null)
                .gradePoint(r.getGradePoint())
                .credits(r.getCredits())
                .status(r.getStatus().name())
                .build();
    }
}
