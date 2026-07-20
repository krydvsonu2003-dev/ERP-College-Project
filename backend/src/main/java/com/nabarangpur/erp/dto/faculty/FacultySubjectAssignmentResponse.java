package com.nabarangpur.erp.dto.faculty;

import com.nabarangpur.erp.entity.FacultySubjectAssignment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder @AllArgsConstructor
public class FacultySubjectAssignmentResponse {
    private Long id;
    private Long facultyId;
    private String facultyName;
    private Long subjectId;
    private String subjectName;
    private Long classSectionId;

    public static FacultySubjectAssignmentResponse from(FacultySubjectAssignment a) {
        return FacultySubjectAssignmentResponse.builder()
                .id(a.getId())
                .facultyId(a.getFaculty().getId())
                .facultyName(a.getFaculty().getUser().getFullName())
                .subjectId(a.getSubject().getId())
                .subjectName(a.getSubject().getName())
                .classSectionId(a.getClassSection().getId())
                .build();
    }
}
