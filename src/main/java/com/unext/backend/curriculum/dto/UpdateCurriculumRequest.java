package com.unext.backend.curriculum.dto;

import com.unext.backend.curriculum.entity.CurriculumStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record UpdateCurriculumRequest(
        @Size(max = 200)
        String curriculumNameTh,

        @Size(max = 200)
        String curriculumNameEn,

        @Size(max = 50)
        String degreeLevel,

        Long facultyId,

        Long departmentId,

        @Min(value = 60, message = "จำนวนหน่วยกิตต้องอยู่ระหว่าง 60 ถึง 180")
        @Max(value = 180, message = "จำนวนหน่วยกิตต้องอยู่ระหว่าง 60 ถึง 180")
        Integer totalCredits,

        @DecimalMin(value = "1.0") @DecimalMax(value = "8.0")
        BigDecimal durationYears,

        @Min(value = 2500)
        Integer effectiveYear,

        @Size(max = 100)
        String accreditationBody,

        CurriculumStatus status,

        String description
) {}
