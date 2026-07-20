package com.nabarangpur.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "admission_status_history")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdmissionStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private AdmissionApplication application;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 20)
    private AdmissionStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 20)
    private AdmissionStatus toStatus;

    @Column(columnDefinition = "CLOB")
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private User changedBy;

    @Column(name = "changed_at", nullable = true, updatable = false)
    @CreationTimestamp
    private Instant changedAt;
}
