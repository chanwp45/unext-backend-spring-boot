package com.unext.backend.student.controller;

import com.unext.backend.audit.service.AuditContext;
import com.unext.backend.audit.service.AuditLogService;
import com.unext.backend.shared.response.ApiResponse;
import com.unext.backend.shared.response.PagedData;
import com.unext.backend.student.dto.*;
import com.unext.backend.student.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Tag(name = "students", description = "Student registration management (PGM-002)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/v1/students")
public class StudentController {

    @Autowired private StudentService studentService;
    @Autowired private AuditLogService auditLogService;

    @Operation(summary = "F-S01: Search students")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<PagedData<StudentResponse>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long curriculumId,
            @RequestParam(required = false) Integer admissionYear,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit) {

        PagedData<StudentResponse> result =
                studentService.search(keyword, status, curriculumId, admissionYear, page, limit);
        return ResponseEntity.ok(ApiResponse.ok(result, "OK"));
    }

    @Operation(summary = "F-S01: Get student by ID")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Student found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Token expired or invalid")
    })
    @GetMapping("/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF') or (hasRole('STUDENT') and #studentId == authentication.principal)")
    public ResponseEntity<ApiResponse<StudentResponse>> findById(@PathVariable String studentId) {
        return ResponseEntity.ok(ApiResponse.ok(studentService.findById(studentId)));
    }

    @Operation(summary = "F-S02: Register a new student")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Student registered"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "เลขบัตรประชาชนนี้มีในระบบแล้ว"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Validation error")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<StudentResponse>> register(
            @Valid @RequestBody CreateStudentRequest request,
            Authentication auth, HttpServletRequest httpRequest) {

        AuditContext ctx = AuditContext.of(auth, httpRequest);
        StudentResponse created = studentService.register(request, ctx);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.studentId()).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.created(created, "Student registered successfully"));
    }

    @Operation(summary = "F-S03: Update student information",
               description = "Staff/Admin can update all fields. Student can only update address, phone, email.")
    @PatchMapping("/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF') or (hasRole('STUDENT') and #studentId == authentication.principal)")
    public ResponseEntity<ApiResponse<StudentResponse>> update(
            @PathVariable String studentId,
            @Valid @RequestBody UpdateStudentRequest request,
            Authentication auth, HttpServletRequest httpRequest) {

        boolean isStaff = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_STAFF"));

        AuditContext ctx = AuditContext.of(auth, httpRequest);
        return ResponseEntity.ok(ApiResponse.ok(
                studentService.update(studentId, request, isStaff, ctx),
                "Student information updated successfully"));
    }

    @Operation(summary = "F-S04: Change student status")
    @PatchMapping("/{studentId}/status")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<StudentResponse>> updateStatus(
            @PathVariable String studentId,
            @Valid @RequestBody UpdateStudentStatusRequest request,
            Authentication auth, HttpServletRequest httpRequest) {

        AuditContext ctx = AuditContext.of(auth, httpRequest)
                .withReason(request.reason(), request.documentRef());
        return ResponseEntity.ok(ApiResponse.ok(
                studentService.updateStatus(studentId, request, ctx), "Student status updated"));
    }

    @Operation(summary = "F-S05: Update student photo URL")
    @PatchMapping("/{studentId}/photo")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF') or (hasRole('STUDENT') and #studentId == authentication.principal)")
    public ResponseEntity<ApiResponse<StudentResponse>> updatePhoto(
            @PathVariable String studentId,
            @RequestBody PhotoRequest body,
            Authentication auth, HttpServletRequest httpRequest) {

        AuditContext ctx = AuditContext.of(auth, httpRequest);
        return ResponseEntity.ok(ApiResponse.ok(
                studentService.updatePhoto(studentId, body.photoUrl(), ctx),
                "Photo updated successfully"));
    }

    @Operation(summary = "F-S06: View audit history for a student")
    @GetMapping("/{studentId}/audit")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<PagedData<AuditLogService.AuditLogEntry>>> getAuditHistory(
            @PathVariable String studentId,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit) {

        studentService.findById(studentId);
        PagedData<AuditLogService.AuditLogEntry> history =
                auditLogService.getHistory("students", studentId, page, limit);
        return ResponseEntity.ok(ApiResponse.ok(history, "OK"));
    }

    public record PhotoRequest(@NotBlank String photoUrl) {}
}
