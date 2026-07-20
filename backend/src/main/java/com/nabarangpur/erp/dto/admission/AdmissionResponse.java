package com.nabarangpur.erp.dto.admission;

import com.nabarangpur.erp.entity.AdmissionApplication;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class AdmissionResponse {
    private Long id;
    private String admissionRefNo;
    private String fullName;
    private String gender;
    private LocalDate dateOfBirth;
    private String mobileNumber;
    private String email;
    private String category;
    private Long courseId;
    private String courseName;
    private Integer entrySemester;
    private String status;
    private String rejectionRemarks;
    private String reviewedByName;
    private Instant reviewedAt;
    private Instant createdAt;

    public static AdmissionResponse from(AdmissionApplication a) {
        return AdmissionResponse.builder()
                .id(a.getId())
                .admissionRefNo(a.getAdmissionRefNo())
                .fullName(a.getFullName())
                .gender(a.getGender().name())
                .dateOfBirth(a.getDateOfBirth())
                .mobileNumber(a.getMobileNumber())
                .email(a.getEmail())
                .category(a.getCategory())
                .courseId(a.getCourse().getId())
                .courseName(a.getCourse().getName())
                .entrySemester(a.getEntrySemester())
                .status(a.getStatus().name())
                .rejectionRemarks(a.getRejectionRemarks())
                .reviewedByName(a.getReviewedBy() != null ? a.getReviewedBy().getFullName() : null)
                .reviewedAt(a.getReviewedAt())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
