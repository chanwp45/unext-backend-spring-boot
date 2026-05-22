package com.unext.backend.student.dto;

import com.unext.backend.student.entity.Gender;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * Staff can update any field. Students can only update address, phone, email
 * (enforced in the service layer).
 */
public record UpdateStudentRequest(
        @Size(max = 20)
        String titleTh,

        @Size(max = 100)
        String firstNameTh,

        @Size(max = 100)
        String lastNameTh,

        @Size(max = 100)
        String firstNameEn,

        @Size(max = 100)
        String lastNameEn,

        @Past(message = "วันเกิดต้องเป็นวันในอดีต")
        LocalDate dateOfBirth,

        Gender gender,

        @Size(max = 50)
        String nationality,

        @Email(message = "รูปแบบอีเมลไม่ถูกต้อง")
        @Size(max = 150)
        String email,

        @Size(max = 20)
        String phone,

        String address,

        Long curriculumId,

        @Size(max = 200)
        String guardianName,

        @Size(max = 20)
        String guardianPhone
) {}
