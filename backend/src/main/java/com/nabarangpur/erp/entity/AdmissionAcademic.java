package com.nabarangpur.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "admission_academics")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdmissionAcademic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false, unique = true)
    private AdmissionApplication application;

    @Column(name = "previous_institution", length = 255)
    private String previousInstitution;

    @Column(length = 100)
    private String qualification;

    @Column(name = "board_university", length = 150)
    private String boardUniversity;

    @Column(name = "year_of_passing")
    private Integer yearOfPassing;

    @Column(name = "marks_percentage")
    private BigDecimal marksPercentage;
}
