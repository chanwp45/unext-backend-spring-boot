package com.unext.backend.faculty.repository;

import com.unext.backend.faculty.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findAllByFacultyIdAndActiveTrueOrderByNameTh(Long facultyId);
    Optional<Department> findByCode(String code);
}
