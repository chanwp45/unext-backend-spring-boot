package com.unext.backend.curriculum.dto;

import com.unext.backend.curriculum.entity.Curriculum;

import java.math.BigDecimal;
import java.time.Instant;

public record CurriculumResponse(
        Long curriculumId,
        String curriculumCode,
        String curriculumNameTh,
        String curriculumNameEn,
        String degreeLevel,
        Long facultyId,
        String facultyName,
        Long departmentId,
        String departmentName,
        Integer totalCredits,
        BigDecimal durationYears,
        Integer effectiveYear,
        String accreditationBody,
        String status,
        String description,
        String createdBy,
        String updatedBy,
        Instant createdAt,
        Instant updatedAt
) {
    public static CurriculumResponse from(Curriculum c) {
        return new CurriculumResponse(
                c.getId(),
                c.getCurriculumCode(),
                c.getCurriculumNameTh(),
                c.getCurriculumNameEn(),
                c.getDegreeLevel(),
                c.getFaculty().getId(),
                c.getFaculty().getNameTh(),
                c.getDepartment().getId(),
                c.getDepartment().getNameTh(),
                c.getTotalCredits(),
                c.getDurationYears(),
                c.getEffectiveYear(),
                c.getAccreditationBody(),
                c.getStatus().name(),
                c.getDescription(),
                c.getCreatedBy(),
                c.getUpdatedBy(),
                c.getCreatedAt(),
                c.getUpdatedAt());
    }
}
