package com.unext.backend.faculty.dto;

import com.unext.backend.faculty.entity.Faculty;

public record FacultyResponse(Long id, String code, String nameTh, String nameEn) {
    public static FacultyResponse from(Faculty f) {
        return new FacultyResponse(f.getId(), f.getCode(), f.getNameTh(), f.getNameEn());
    }
}
