package com.unext.backend.faculty.repository;

import com.unext.backend.faculty.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    List<Faculty> findAllByActiveTrueOrderByNameTh();
}
