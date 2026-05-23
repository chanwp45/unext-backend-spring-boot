package com.unext.backend.curriculum.entity;

import com.unext.backend.faculty.entity.Department;
import com.unext.backend.faculty.entity.Faculty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "curricula")
@SQLRestriction("deleted_at IS NULL")
public class Curriculum {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "curriculum_code", nullable = false, unique = true, length = 20)
    private String curriculumCode;

    @Column(name = "curriculum_name_th", nullable = false, length = 200)
    private String curriculumNameTh;

    @Column(name = "curriculum_name_en", nullable = false, length = 200)
    private String curriculumNameEn;

    @Column(name = "degree_level", nullable = false, length = 50)
    private String degreeLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "total_credits", nullable = false)
    private Integer totalCredits;

    @Column(name = "duration_years", nullable = false, precision = 3, scale = 1)
    private BigDecimal durationYears;

    @Column(name = "effective_year", nullable = false)
    private Integer effectiveYear;

    @Column(name = "accreditation_body", length = 100)
    private String accreditationBody;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CurriculumStatus status = CurriculumStatus.DRAFT;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @PreUpdate
    void onUpdate() { this.updatedAt = Instant.now(); }
}
