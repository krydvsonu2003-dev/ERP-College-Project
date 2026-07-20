package com.nabarangpur.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "fee_waivers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FeeWaiver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_fee_assignment_id", nullable = false)
    private StudentFeeAssignment studentFeeAssignment;

    @Column(name = "waiver_amount", nullable = false)
    private BigDecimal waiverAmount;

    @Column(nullable = false, length = 255)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by", nullable = false)
    private User approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;
}
