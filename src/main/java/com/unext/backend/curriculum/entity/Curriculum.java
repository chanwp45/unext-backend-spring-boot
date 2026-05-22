package com.unext.backend.curriculum.entity;

import com.unext.backend.faculty.entity.Department;
import com.unext.backend.faculty.entity.Faculty;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "curricula")
@SQLRestriction("deleted_at IS NULL")
public class Curriculum {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    public Long getId() { return id; }
    public String getCurriculumCode() { return curriculumCode; }
    public void setCurriculumCode(String curriculumCode) { this.curriculumCode = curriculumCode; }
    public String getCurriculumNameTh() { return curriculumNameTh; }
    public void setCurriculumNameTh(String curriculumNameTh) { this.curriculumNameTh = curriculumNameTh; }
    public String getCurriculumNameEn() { return curriculumNameEn; }
    public void setCurriculumNameEn(String curriculumNameEn) { this.curriculumNameEn = curriculumNameEn; }
    public String getDegreeLevel() { return degreeLevel; }
    public void setDegreeLevel(String degreeLevel) { this.degreeLevel = degreeLevel; }
    public Faculty getFaculty() { return faculty; }
    public void setFaculty(Faculty faculty) { this.faculty = faculty; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public Integer getTotalCredits() { return totalCredits; }
    public void setTotalCredits(Integer totalCredits) { this.totalCredits = totalCredits; }
    public BigDecimal getDurationYears() { return durationYears; }
    public void setDurationYears(BigDecimal durationYears) { this.durationYears = durationYears; }
    public Integer getEffectiveYear() { return effectiveYear; }
    public void setEffectiveYear(Integer effectiveYear) { this.effectiveYear = effectiveYear; }
    public String getAccreditationBody() { return accreditationBody; }
    public void setAccreditationBody(String accreditationBody) { this.accreditationBody = accreditationBody; }
    public CurriculumStatus getStatus() { return status; }
    public void setStatus(CurriculumStatus status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
}
