package com.nabarangpur.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "grade_master")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GradeMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "grade_letter", nullable = false, unique = true, length = 5)
    private String gradeLetter;

    @Column(name = "min_percentage", nullable = false)
    private BigDecimal minPercentage;

    @Column(name = "max_percentage", nullable = false)
    private BigDecimal maxPercentage;

    @Column(name = "grade_point", nullable = false)
    private BigDecimal gradePoint;

    @Column(name = "is_pass", nullable = false, columnDefinition = "NUMBER(1)")
    @Builder.Default
    private boolean pass = true;
}
