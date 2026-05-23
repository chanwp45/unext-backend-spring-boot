package com.unext.backend.faculty.controller;

import com.unext.backend.faculty.dto.DepartmentResponse;
import com.unext.backend.faculty.dto.FacultyResponse;
import com.unext.backend.faculty.service.FacultyService;
import com.unext.backend.shared.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "faculties", description = "Faculty and Department lookup")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/v1/faculties")
public class FacultyController {

    @Autowired private FacultyService facultyService;

    @Operation(summary = "List all active faculties")
    @GetMapping
    public ResponseEntity<ApiResponse<List<FacultyResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.ok(facultyService.findAll(), "OK"));
    }

    @Operation(summary = "List departments under a faculty")
    @GetMapping("/{id}/departments")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> findDepartments(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(facultyService.findDepartmentsByFaculty(id), "OK"));
    }
}
