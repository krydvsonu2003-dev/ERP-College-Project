package com.nabarangpur.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "class_sections")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ClassSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @Column(nullable = false)
    private Integer semester;

    @Column(name = "section_name", nullable = false, length = 10)
    @Builder.Default
    private String sectionName = "A";

    @Column(name = "created_at")
    private Instant createdAt;
}
