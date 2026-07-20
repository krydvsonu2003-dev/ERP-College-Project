package com.nabarangpur.erp.dto.admission;

import com.nabarangpur.erp.entity.Student;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class StudentResponse {
    private Long id;
    private String studentCode;
    private String fullName;
    private String gender;
    private LocalDate dateOfBirth;
    private String mobileNumber;
    private String email;
    private String category;
    private Long courseId;
    private String courseName;
    private Long departmentId;
    private String departmentName;
    private Integer currentSemester;
    private String academicYearName;
    private String status;
    private LocalDate admittedOn;

    public static StudentResponse from(Student s) {
        return StudentResponse.builder()
                .id(s.getId())
                .studentCode(s.getStudentCode())
                .fullName(s.getFullName())
                .gender(s.getGender().name())
                .dateOfBirth(s.getDateOfBirth())
                .mobileNumber(s.getMobileNumber())
                .email(s.getEmail())
                .category(s.getCategory())
                .courseId(s.getCourse().getId())
                .courseName(s.getCourse().getName())
                .departmentId(s.getDepartment().getId())
                .departmentName(s.getDepartment().getName())
                .currentSemester(s.getCurrentSemester())
                .academicYearName(s.getAcademicYear().getName())
                .status(s.getStatus().name())
                .admittedOn(s.getAdmittedOn())
                .build();
    }
}
