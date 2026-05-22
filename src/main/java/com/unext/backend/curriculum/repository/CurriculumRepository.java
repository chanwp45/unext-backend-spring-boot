package com.unext.backend.curriculum.repository;

import com.unext.backend.curriculum.entity.Curriculum;
import com.unext.backend.curriculum.entity.CurriculumStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CurriculumRepository extends JpaRepository<Curriculum, Long> {

    @Query("""
        SELECT c FROM Curriculum c
        WHERE (:degreeLevel IS NULL OR c.degreeLevel = :degreeLevel)
          AND (:status IS NULL OR c.status = :status)
          AND (:facultyId IS NULL OR c.faculty.id = :facultyId)
          AND (:keyword IS NULL OR LOWER(c.curriculumNameTh) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(c.curriculumNameEn) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(c.curriculumCode) LIKE LOWER(CONCAT('%', :keyword, '%')))
        """)
    Page<Curriculum> search(
            @Param("degreeLevel") String degreeLevel,
            @Param("status") CurriculumStatus status,
            @Param("facultyId") Long facultyId,
            @Param("keyword") String keyword,
            Pageable pageable);

    int countByCurriculumCodeStartingWithAndEffectiveYear(String codePrefix, Integer effectiveYear);

    boolean existsByCurriculumCode(String curriculumCode);
}
