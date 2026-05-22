package com.unext.backend.student.entity;

import com.unext.backend.curriculum.entity.Curriculum;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "students")
@SQLRestriction("deleted_at IS NULL")
public class Student {

    @Id
    @Column(name = "student_id", length = 15)
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

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getNationalId() { return nationalId; }
    public void setNationalId(String nationalId) { this.nationalId = nationalId; }
    public String getTitleTh() { return titleTh; }
    public void setTitleTh(String titleTh) { this.titleTh = titleTh; }
    public String getFirstNameTh() { return firstNameTh; }
    public void setFirstNameTh(String firstNameTh) { this.firstNameTh = firstNameTh; }
    public String getLastNameTh() { return lastNameTh; }
    public void setLastNameTh(String lastNameTh) { this.lastNameTh = lastNameTh; }
    public String getFirstNameEn() { return firstNameEn; }
    public void setFirstNameEn(String firstNameEn) { this.firstNameEn = firstNameEn; }
    public String getLastNameEn() { return lastNameEn; }
    public void setLastNameEn(String lastNameEn) { this.lastNameEn = lastNameEn; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }
    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Curriculum getCurriculum() { return curriculum; }
    public void setCurriculum(Curriculum curriculum) { this.curriculum = curriculum; }
    public Integer getAdmissionYear() { return admissionYear; }
    public void setAdmissionYear(Integer admissionYear) { this.admissionYear = admissionYear; }
    public StudentStatus getStudentStatus() { return studentStatus; }
    public void setStudentStatus(StudentStatus studentStatus) { this.studentStatus = studentStatus; }
    public String getGuardianName() { return guardianName; }
    public void setGuardianName(String guardianName) { this.guardianName = guardianName; }
    public String getGuardianPhone() { return guardianPhone; }
    public void setGuardianPhone(String guardianPhone) { this.guardianPhone = guardianPhone; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
}
