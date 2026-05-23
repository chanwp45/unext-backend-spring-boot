package com.unext.backend.curriculum.controller;

import com.unext.backend.audit.service.AuditContext;
import com.unext.backend.audit.service.AuditLogService;
import com.unext.backend.curriculum.dto.CreateCurriculumRequest;
import com.unext.backend.curriculum.dto.CurriculumResponse;
import com.unext.backend.curriculum.dto.UpdateCurriculumRequest;
import com.unext.backend.curriculum.service.CurriculumService;
import com.unext.backend.shared.response.ApiResponse;
import com.unext.backend.shared.response.PagedData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Tag(name = "curricula", description = "Curriculum Master management (PGM-001)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/v1/curricula")
public class CurriculumController {

    @Autowired private CurriculumService curriculumService;
    @Autowired private AuditLogService auditLogService;

    @Operation(summary = "F-C01: Search/list curricula with filters")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<PagedData<CurriculumResponse>>> search(
            @RequestParam(required = false) String degreeLevel,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long facultyId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit) {

        PagedData<CurriculumResponse> result =
                curriculumService.search(degreeLevel, status, facultyId, keyword, page, limit);
        return ResponseEntity.ok(ApiResponse.ok(result, "OK"));
    }

    @Operation(summary = "F-C02: Get curriculum by ID")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Curriculum found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<CurriculumResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(curriculumService.findById(id)));
    }

    @Operation(summary = "F-C02: Create new curriculum")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Curriculum created"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ไม่มีสิทธิ์ดำเนินการ"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Validation error")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<CurriculumResponse>> create(
            @Valid @RequestBody CreateCurriculumRequest request,
            Authentication auth, HttpServletRequest httpRequest) {

        AuditContext ctx = AuditContext.of(auth, httpRequest);
        CurriculumResponse created = curriculumService.create(request, ctx);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.curriculumId()).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.created(created, "Curriculum created successfully"));
    }

    @Operation(summary = "F-C03: Update curriculum")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<CurriculumResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCurriculumRequest request,
            Authentication auth, HttpServletRequest httpRequest) {

        AuditContext ctx = AuditContext.of(auth, httpRequest);
        return ResponseEntity.ok(ApiResponse.ok(
                curriculumService.update(id, request, ctx), "Curriculum updated successfully"));
    }

    @Operation(summary = "F-C04: Soft-delete curriculum (set status to INACTIVE)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<Void>> softDelete(
            @PathVariable Long id,
            Authentication auth, HttpServletRequest httpRequest) {

        AuditContext ctx = AuditContext.of(auth, httpRequest);
        curriculumService.softDelete(id, ctx);
        return ResponseEntity.ok(ApiResponse.ok(null, "Curriculum deactivated successfully"));
    }

    @Operation(summary = "F-C05: View audit history for a curriculum")
    @GetMapping("/{id}/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagedData<AuditLogService.AuditLogEntry>>> getAuditHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit) {

        // Verify curriculum exists first
        curriculumService.findById(id);
        PagedData<AuditLogService.AuditLogEntry> history =
                auditLogService.getHistory("curricula", id.toString(), page, limit);
        return ResponseEntity.ok(ApiResponse.ok(history, "OK"));
    }
}
