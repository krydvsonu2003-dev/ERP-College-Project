package com.nabarangpur.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "student_guardians")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentGuardian {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false, unique = true)
    private Student student;

    @Column(name = "father_name", length = 150)
    private String fatherName;

    @Column(name = "mother_name", length = 150)
    private String motherName;

    @Column(name = "guardian_name", length = 150)
    private String guardianName;

    @Column(name = "guardian_contact", length = 20)
    private String guardianContact;

    @Column(length = 100)
    private String occupation;

    @Column(name = "annual_income")
    private BigDecimal annualIncome;
}
