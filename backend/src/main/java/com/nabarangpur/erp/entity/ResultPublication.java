package com.nabarangpur.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "result_publications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ResultPublication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "examination_id", nullable = false)
    private Examination examination;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "published_by", nullable = false)
    private User publishedBy;

    @Column(name = "published_at", nullable = false)
    private Instant publishedAt;
    @PrePersist
    public void prePersist() {
        if (publishedAt == null) {
            publishedAt = Instant.now();
        }
    }

    @Column(length = 255)
    private String remarks;
}
