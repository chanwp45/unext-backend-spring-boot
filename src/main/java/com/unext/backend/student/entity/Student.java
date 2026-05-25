package com.unext.backend.student.entity;

import com.unext.backend.curriculum.entity.Curriculum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "students")
@SQLRestriction("deleted_at IS NULL")
public class Student {

    @Id
    @Column(name = "student_id", length = 15)
    @EqualsAndHashCode.Include
    private String studentId;

    @Column(name = "national_id", nullable = false, unique = true, length = 13)
    private String nationalId;

    @Column(name = "title_th", nullable = false, length = 20)
    private String titleTh;

    @Column(name = "first_name_th", nullable = false, length = 100)
    private String firstNameTh;

    @Column(name = "last_name_th", nullable = false, length = 100)
    private String lastNameTh;

    @Column(name = "first_name_en", nullable = false, length = 100)
    private String firstNameEn;

    @Column(name = "last_name_en", nullable = false, length = 100)
    private String lastNameEn;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;

    @Column(nullable = false, length = 50)
    private String nationality;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curriculum_id", nullable = false)
    private Curriculum curriculum;

    @Column(name = "admission_year", nullable = false)
    private Integer admissionYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "student_status", nullable = false, length = 20)
    private StudentStatus studentStatus = StudentStatus.STUDYING;

    @Column(name = "guardian_name", length = 200)
    private String guardianName;

    @Column(name = "guardian_phone", length = 20)
    private String guardianPhone;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

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
