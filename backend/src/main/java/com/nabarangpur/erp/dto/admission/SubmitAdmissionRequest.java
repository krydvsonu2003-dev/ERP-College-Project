package com.nabarangpur.erp.dto.admission;

import com.nabarangpur.erp.entity.Gender;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class SubmitAdmissionRequest {

    // Personal details
    @NotBlank private String fullName;
    @NotNull  private Gender gender;
    @NotNull  private LocalDate dateOfBirth;
    @NotBlank private String mobileNumber;
    @Email    private String email;
    private String address;
    private String category;
    private String idProofNumber;

    // Academic intent
    @NotNull private Long courseId;
    @NotNull private Long academicYearId;
    @NotNull @Min(1) private Integer entrySemester;

    // Guardian details
    private String fatherName;
    private String motherName;
    private String guardianName;
    private String guardianContact;
    private String occupation;
    private BigDecimal annualIncome;

    // Previous academic details
    private String previousInstitution;
    private String qualification;
    private String boardUniversity;
    private Integer yearOfPassing;
    private BigDecimal marksPercentage;
}
