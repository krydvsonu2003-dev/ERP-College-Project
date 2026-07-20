package com.nabarangpur.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "faculty_profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FacultyProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "employee_code", unique = true, length = 30)
    private String employeeCode;

    @Column(length = 100)
    private String designation;

    @Column(name = "created_at")
    private Instant createdAt;
}
