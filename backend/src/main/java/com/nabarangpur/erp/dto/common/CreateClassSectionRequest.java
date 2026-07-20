package com.nabarangpur.erp.dto.common;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreateClassSectionRequest {
    @NotNull private Long courseId;
    @NotNull private Long academicYearId;
    @NotNull private Integer semester;
    private String sectionName = "A";
}
