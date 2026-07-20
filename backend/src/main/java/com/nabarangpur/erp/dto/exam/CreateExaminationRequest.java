package com.nabarangpur.erp.dto.exam;

import com.nabarangpur.erp.entity.ExamType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateExaminationRequest {
    @NotBlank private String name;
    @NotNull private Long courseId;
    @NotNull private Long academicYearId;
    @NotNull private Integer semester;
    private ExamType examType = ExamType.FINAL;
    private LocalDate startDate;
    private LocalDate endDate;
}
