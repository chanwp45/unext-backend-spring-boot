package com.unext.backend.faculty.service;

import com.unext.backend.faculty.dto.DepartmentResponse;
import com.unext.backend.faculty.dto.FacultyResponse;
import com.unext.backend.faculty.entity.Department;
import com.unext.backend.faculty.entity.Faculty;
import com.unext.backend.faculty.repository.DepartmentRepository;
import com.unext.backend.faculty.repository.FacultyRepository;
import com.unext.backend.shared.exception.AppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class FacultyServiceTest {

    @Mock private FacultyRepository facultyRepository;
    @Mock private DepartmentRepository departmentRepository;

    @InjectMocks private FacultyService facultyService;

    private Faculty faculty;
    private Department department;

    @BeforeEach
    void setUp() {
        faculty = new Faculty();
        faculty.setCode("ENG");
        faculty.setNameTh("คณะวิศวกรรมศาสตร์");
        faculty.setNameEn("Faculty of Engineering");

        department = new Department();
        department.setCode("CS");
        department.setNameTh("สาขาวิทยาการคอมพิวเตอร์");
        department.setNameEn("Computer Science");
        department.setFaculty(faculty);
    }

    // ─── findAll ────────────────────────────────────────────────────────────

    @Test
    void findAll_shouldReturnListOfActiveFaculties() {
        // Arrange
        given(facultyRepository.findAllByActiveTrueOrderByNameTh()).willReturn(List.of(faculty));

        // Act
        List<FacultyResponse> result = facultyService.findAll();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).code()).isEqualTo("ENG");
        assertThat(result.get(0).nameTh()).isEqualTo("คณะวิศวกรรมศาสตร์");
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoActiveFaculties() {
        // Arrange
        given(facultyRepository.findAllByActiveTrueOrderByNameTh()).willReturn(List.of());

        // Act
        List<FacultyResponse> result = facultyService.findAll();

        // Assert
        assertThat(result).isEmpty();
    }

    // ─── findDepartmentsByFaculty ─────────────────────────────────────────

    @Test
    void findDepartmentsByFaculty_shouldReturnDepartments_whenFacultyExists() {
        // Arrange
        given(facultyRepository.existsById(1L)).willReturn(true);
        given(departmentRepository.findAllByFacultyIdAndActiveTrueOrderByNameTh(1L))
                .willReturn(List.of(department));

        // Act
        List<DepartmentResponse> result = facultyService.findDepartmentsByFaculty(1L);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).code()).isEqualTo("CS");
        assertThat(result.get(0).nameTh()).isEqualTo("สาขาวิทยาการคอมพิวเตอร์");
    }

    @Test
    void findDepartmentsByFaculty_shouldReturnEmptyList_whenFacultyHasNoDepartments() {
        // Arrange
        given(facultyRepository.existsById(1L)).willReturn(true);
        given(departmentRepository.findAllByFacultyIdAndActiveTrueOrderByNameTh(1L))
                .willReturn(List.of());

        // Act
        List<DepartmentResponse> result = facultyService.findDepartmentsByFaculty(1L);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void findDepartmentsByFaculty_shouldThrowNotFound_whenFacultyDoesNotExist() {
        // Arrange
        given(facultyRepository.existsById(999L)).willReturn(false);

        // Act / Assert
        assertThatThrownBy(() -> facultyService.findDepartmentsByFaculty(999L))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", "FACULTY_NOT_FOUND");
    }
}
