package com.unext.backend.student.dto;

import com.unext.backend.student.entity.Gender;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record CreateStudentRequest(
        @NotBlank(message = "เลขบัตรประชาชนต้องมี 13 หลัก")
        @Pattern(regexp = "\\d{13}", message = "เลขบัตรประชาชนต้องมี 13 หลัก")
        String nationalId,

        @NotBlank(message = "กรุณาระบุคำนำหน้าชื่อ")
        @Size(max = 20)
        String titleTh,

        @NotBlank(message = "กรุณากรอกชื่อภาษาไทย")
        @Size(max = 100)
        String firstNameTh,

        @NotBlank(message = "กรุณากรอกนามสกุลภาษาไทย")
        @Size(max = 100)
        String lastNameTh,

        @NotBlank(message = "กรุณากรอกชื่อภาษาอังกฤษ")
        @Size(max = 100)
        String firstNameEn,

        @NotBlank(message = "กรุณากรอกนามสกุลภาษาอังกฤษ")
        @Size(max = 100)
        String lastNameEn,

        @NotNull(message = "กรุณาระบุวันเกิด")
        @Past(message = "วันเกิดต้องเป็นวันในอดีต")
        LocalDate dateOfBirth,

        @NotNull(message = "กรุณาระบุเพศ")
        Gender gender,

        @NotBlank(message = "กรุณาระบุสัญชาติ")
        @Size(max = 50)
        String nationality,

        @NotBlank(message = "กรุณาระบุอีเมล")
        @Email(message = "รูปแบบอีเมลไม่ถูกต้อง")
        @Size(max = 150)
        String email,

        @NotBlank(message = "กรุณาระบุเบอร์โทรศัพท์")
        @Size(max = 20)
        String phone,

        @NotBlank(message = "กรุณาระบุที่อยู่")
        String address,

        @NotNull(message = "กรุณาระบุหลักสูตร")
        Long curriculumId,

        @NotNull(message = "กรุณาระบุปีการศึกษาที่เข้า")
        @Min(value = 2500, message = "ปีการศึกษาไม่ถูกต้อง")
        Integer admissionYear,

        @Size(max = 200)
        String guardianName,

        @Size(max = 20)
        String guardianPhone
) {}
