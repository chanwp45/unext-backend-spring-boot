package com.unext.backend.curriculum.dto;

import com.unext.backend.curriculum.entity.CurriculumStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateCurriculumRequest(
        @NotBlank(message = "กรุณากรอกชื่อหลักสูตรภาษาไทย")
        @Size(max = 200)
        String curriculumNameTh,

        @NotBlank(message = "กรุณากรอกชื่อหลักสูตรภาษาอังกฤษ")
        @Size(max = 200)
        String curriculumNameEn,

        @NotBlank(message = "กรุณาระบุระดับการศึกษา")
        @Size(max = 50)
        String degreeLevel,

        @NotNull(message = "กรุณาระบุคณะ")
        Long facultyId,

        @NotNull(message = "กรุณาระบุสาขาวิชา")
        Long departmentId,

        @NotNull(message = "กรุณาระบุจำนวนหน่วยกิต")
        @Min(value = 60, message = "จำนวนหน่วยกิตต้องอยู่ระหว่าง 60 ถึง 180")
        @Max(value = 180, message = "จำนวนหน่วยกิตต้องอยู่ระหว่าง 60 ถึง 180")
        Integer totalCredits,

        @NotNull(message = "กรุณาระบุระยะเวลาการศึกษา")
        @DecimalMin(value = "1.0", message = "ระยะเวลาการศึกษาต้องไม่น้อยกว่า 1 ปี")
        @DecimalMax(value = "8.0", message = "ระยะเวลาการศึกษาต้องไม่เกิน 8 ปี")
        BigDecimal durationYears,

        @NotNull(message = "กรุณาระบุปีที่เริ่มใช้หลักสูตร")
        @Min(value = 2500, message = "ปีที่เริ่มใช้หลักสูตรต้องไม่น้อยกว่าปีปัจจุบัน")
        Integer effectiveYear,

        @Size(max = 100)
        String accreditationBody,

        CurriculumStatus status,

        String description
) {}
