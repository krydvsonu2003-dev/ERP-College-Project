package com.nabarangpur.erp.dto.faculty;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AssignFacultySubjectRequest {
    @NotNull private Long facultyId;
    @NotNull private Long subjectId;
    @NotNull private Long classSectionId;
}
