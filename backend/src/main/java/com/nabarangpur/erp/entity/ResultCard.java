package com.nabarangpur.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "result_cards")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ResultCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "examination_id", nullable = false)
    private Examination examination;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "total_marks_obtained", nullable = false)
    private BigDecimal totalMarksObtained;

    @Column(name = "total_max_marks", nullable = false)
    private BigDecimal totalMaxMarks;

    @Column(nullable = false)
    private BigDecimal percentage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_id")
    private GradeMaster grade;

    @Column(name = "grade_point")
    private BigDecimal gradePoint;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal credits = BigDecimal.valueOf(4);

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ResultStatus status = ResultStatus.DRAFT;

    @Column(nullable = false)
    @Builder.Default
    private Integer version = 1;

    @Column(name = "computed_at", nullable = false)
    private Instant computedAt;

    @PrePersist
    public void prePersist() {
        if (computedAt == null) {
            computedAt = Instant.now();
        }
    }
}
