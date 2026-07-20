package com.nabarangpur.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "student_fee_assignments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentFeeAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fee_structure_id", nullable = false)
    private FeeStructure feeStructure;

    @Column(name = "applicable_amount", nullable = false)
    private BigDecimal applicableAmount;

    @Column(name = "waived_amount", nullable = false)
    @Builder.Default
    private BigDecimal waivedAmount = BigDecimal.ZERO;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt;

    @PrePersist
    public void prePersist() {
        if (assignedAt == null) {
            assignedAt = Instant.now();
        }
    }
}
