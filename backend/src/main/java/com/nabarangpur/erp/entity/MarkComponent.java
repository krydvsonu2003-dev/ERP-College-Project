package com.nabarangpur.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "mark_components")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MarkComponent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "weight_percentage", nullable = false)
    private BigDecimal weightPercentage;
}
