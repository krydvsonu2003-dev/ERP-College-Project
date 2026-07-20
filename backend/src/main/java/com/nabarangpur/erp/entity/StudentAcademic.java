package com.nabarangpur.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "student_academics")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentAcademic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false, unique = true)
    private Student student;

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
