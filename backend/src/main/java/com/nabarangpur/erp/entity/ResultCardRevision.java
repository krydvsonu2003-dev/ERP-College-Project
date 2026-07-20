package com.nabarangpur.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "result_card_revisions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ResultCardRevision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_card_id", nullable = false)
    private ResultCard resultCard;

    @Column(name = "previous_marks")
    private BigDecimal previousMarks;

    @Column(name = "previous_grade", length = 5)
    private String previousGrade;

    @Column(length = 255)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "revised_by", nullable = false)
    private User revisedBy;

    @Column(name = "revised_at")
    private Instant revisedAt;
}
