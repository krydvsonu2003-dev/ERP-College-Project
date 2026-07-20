package com.nabarangpur.erp.dto.exam;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class EnterMarksRequest {
    @NotNull private Long examinationId;
    @NotNull private Long subjectId;
    @NotNull private Long studentId;
    @NotNull private Long componentId;
    @NotNull private BigDecimal marksObtained;
    @NotNull private BigDecimal maxMarks;
}
