package com.unext.backend.student.dto;

import com.unext.backend.student.entity.StudentStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateStudentStatusRequest(
        @NotNull(message = "กรุณาระบุสถานะนักศึกษา")
        StudentStatus studentStatus,

        LocalDate effectiveDate,

        String reason,

        String documentRef
) {}
