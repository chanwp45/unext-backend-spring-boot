package com.unext.backend.student.repository;

import com.unext.backend.student.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, String> {

    boolean existsByNationalId(String nationalId);

    boolean existsByEmail(String email);

    Optional<Student> findByNationalId(String nationalId);

    int countByAdmissionYearAndCurriculumId(Integer admissionYear, Long curriculumId);

    @Query("""
        SELECT s FROM Student s
        WHERE (:keyword IS NULL
               OR s.studentId LIKE CONCAT('%', :keyword, '%')
               OR LOWER(s.firstNameTh) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(s.lastNameTh) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(s.firstNameEn) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(s.lastNameEn) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR s.nationalId LIKE CONCAT('%', :keyword, '%')
               OR LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:status IS NULL OR s.studentStatus = :status)
          AND (:curriculumId IS NULL OR s.curriculum.id = :curriculumId)
          AND (:admissionYear IS NULL OR s.admissionYear = :admissionYear)
        """)
    Page<Student> search(
            @Param("keyword") String keyword,
            @Param("status") com.unext.backend.student.entity.StudentStatus status,
            @Param("curriculumId") Long curriculumId,
            @Param("admissionYear") Integer admissionYear,
            Pageable pageable);
}
