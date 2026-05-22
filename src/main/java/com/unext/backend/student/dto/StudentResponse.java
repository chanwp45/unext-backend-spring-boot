package com.unext.backend.student.dto;

import com.unext.backend.student.entity.Student;

import java.time.Instant;
import java.time.LocalDate;

public record StudentResponse(
        String studentId,
        String nationalId,
        String titleTh,
        String firstNameTh,
        String lastNameTh,
        String firstNameEn,
        String lastNameEn,
        LocalDate dateOfBirth,
        String gender,
        String nationality,
        String email,
        String phone,
        String address,
        CurriculumInfo curriculum,
        Integer admissionYear,
        String studentStatus,
        String guardianName,
        String guardianPhone,
        String photoUrl,
        Instant createdAt,
        Instant updatedAt
) {
    public record CurriculumInfo(Long curriculumId, String curriculumCode, String curriculumNameTh) {}

    public static StudentResponse from(Student s) {
        var c = s.getCurriculum();
        return new StudentResponse(
                s.getStudentId(), s.getNationalId(), s.getTitleTh(),
                s.getFirstNameTh(), s.getLastNameTh(), s.getFirstNameEn(), s.getLastNameEn(),
                s.getDateOfBirth(), s.getGender().name(), s.getNationality(),
                s.getEmail(), s.getPhone(), s.getAddress(),
                new CurriculumInfo(c.getId(), c.getCurriculumCode(), c.getCurriculumNameTh()),
                s.getAdmissionYear(), s.getStudentStatus().name(),
                s.getGuardianName(), s.getGuardianPhone(), s.getPhotoUrl(),
                s.getCreatedAt(), s.getUpdatedAt());
    }
}
