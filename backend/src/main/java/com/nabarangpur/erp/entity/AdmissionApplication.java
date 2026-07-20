package com.nabarangpur.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "admission_applications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdmissionApplication extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admission_ref_no", nullable = false, unique = true, length = 30)
    private String admissionRefNo;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "mobile_number", nullable = false, length = 20)
    private String mobileNumber;

    @Column(length = 150)
    private String email;

    @Column(columnDefinition = "text")
    private String address;

    @Column(length = 30)
    private String category;

    @Column(name = "id_proof_number", length = 50)
    private String idProofNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @Column(name = "entry_semester", nullable = false)
    @Builder.Default
    private Integer entrySemester = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AdmissionStatus status = AdmissionStatus.SUBMITTED;

    @Column(name = "rejection_remarks", columnDefinition = "text")
    private String rejectionRemarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "reviewed_at")
   
    private Instant reviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by")
    private User submittedBy;
    
    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private boolean deleted = false;
}
