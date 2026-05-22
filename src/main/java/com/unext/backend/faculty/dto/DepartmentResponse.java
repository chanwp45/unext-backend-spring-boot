package com.unext.backend.faculty.dto;

import com.unext.backend.faculty.entity.Department;

public record DepartmentResponse(Long id, Long facultyId, String code, String nameTh, String nameEn) {
    public static DepartmentResponse from(Department d) {
        return new DepartmentResponse(d.getId(), d.getFaculty().getId(), d.getCode(), d.getNameTh(), d.getNameEn());
    }
}
