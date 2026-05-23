package com.unext.backend.faculty.service;

import com.unext.backend.faculty.dto.DepartmentResponse;
import com.unext.backend.faculty.dto.FacultyResponse;
import com.unext.backend.faculty.repository.DepartmentRepository;
import com.unext.backend.faculty.repository.FacultyRepository;
import com.unext.backend.shared.exception.AppException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FacultyService {

    @Autowired private FacultyRepository facultyRepository;
    @Autowired private DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public List<FacultyResponse> findAll() {
        return facultyRepository.findAllByActiveTrueOrderByNameTh().stream()
                .map(FacultyResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> findDepartmentsByFaculty(Long facultyId) {
        if (!facultyRepository.existsById(facultyId)) {
            throw new AppException("FACULTY_NOT_FOUND",
                    "Faculty with id '" + facultyId + "' was not found.", HttpStatus.NOT_FOUND);
        }
        return departmentRepository.findAllByFacultyIdAndActiveTrueOrderByNameTh(facultyId)
                .stream().map(DepartmentResponse::from).toList();
    }
}

