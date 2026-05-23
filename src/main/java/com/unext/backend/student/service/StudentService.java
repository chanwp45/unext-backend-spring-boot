package com.unext.backend.student.service;

import com.unext.backend.audit.entity.AuditAction;
import com.unext.backend.audit.service.AuditContext;
import com.unext.backend.audit.service.AuditLogService;
import com.unext.backend.curriculum.entity.Curriculum;
import com.unext.backend.curriculum.repository.CurriculumRepository;
import com.unext.backend.shared.exception.AppException;
import com.unext.backend.shared.response.PagedData;
import com.unext.backend.shared.response.PaginationMeta;
import com.unext.backend.student.dto.*;
import com.unext.backend.student.entity.Student;
import com.unext.backend.student.entity.StudentStatus;
import com.unext.backend.student.exception.StudentNotFoundException;
import com.unext.backend.student.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StudentService {

    @Autowired private StudentRepository studentRepository;
    @Autowired private CurriculumRepository curriculumRepository;
    @Autowired private AuditLogService auditLogService;

    /** Search students with optional keyword, status, curriculum, and admission year filters. */
    @Transactional(readOnly = true)
    public PagedData<StudentResponse> search(String keyword, String status,
                                              Long curriculumId, Integer admissionYear,
                                              int page, int limit) {
        StudentStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = StudentStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new AppException("INVALID_STATUS",
                        "Invalid status. Must be one of: STUDYING, LEAVE, RESIGNED, GRADUATED, EXPELLED",
                        HttpStatus.BAD_REQUEST);
            }
        }

        Page<Student> result = studentRepository.search(
                keyword, statusEnum, curriculumId, admissionYear,
                PageRequest.of(page - 1, limit, Sort.by("createdAt").descending()));

        List<StudentResponse> items = result.getContent().stream().map(StudentResponse::from).toList();
        return new PagedData<>(items, PaginationMeta.of(page, limit, result.getTotalElements()));
    }

    /** Returns a student record by student ID. */
    @Transactional(readOnly = true)
    public StudentResponse findById(String studentId) {
        return studentRepository.findById(studentId)
                .map(StudentResponse::from)
                .orElseThrow(() -> new StudentNotFoundException(studentId));
    }

    /** Registers a new student and auto-generates student_id. */
    @Transactional
    public StudentResponse register(CreateStudentRequest req, AuditContext ctx) {
        if (studentRepository.existsByNationalId(req.nationalId())) {
            throw new AppException("NATIONAL_ID_CONFLICT",
                    "เน€เธฅเธเธเธฑเธ•เธฃเธเธฃเธฐเธเธฒเธเธเธเธตเนเธกเธตเนเธเธฃเธฐเธเธเนเธฅเนเธง", HttpStatus.CONFLICT);
        }
        if (studentRepository.existsByEmail(req.email().toLowerCase())) {
            throw new AppException("EMAIL_CONFLICT",
                    "เธญเธตเน€เธกเธฅเธเธตเนเธกเธตเนเธเธฃเธฐเธเธเนเธฅเนเธง", HttpStatus.CONFLICT);
        }

        Curriculum curriculum = curriculumRepository.findById(req.curriculumId())
                .orElseThrow(() -> new AppException("CURRICULUM_NOT_FOUND",
                        "Curriculum not found.", HttpStatus.UNPROCESSABLE_ENTITY));

        String studentId = generateStudentId(req.admissionYear(), req.curriculumId());

        Student student = new Student();
        student.setStudentId(studentId);
        student.setNationalId(req.nationalId());
        student.setTitleTh(req.titleTh());
        student.setFirstNameTh(req.firstNameTh());
        student.setLastNameTh(req.lastNameTh());
        student.setFirstNameEn(req.firstNameEn());
        student.setLastNameEn(req.lastNameEn());
        student.setDateOfBirth(req.dateOfBirth());
        student.setGender(req.gender());
        student.setNationality(req.nationality());
        student.setEmail(req.email().toLowerCase());
        student.setPhone(req.phone());
        student.setAddress(req.address());
        student.setCurriculum(curriculum);
        student.setAdmissionYear(req.admissionYear());
        student.setStudentStatus(StudentStatus.STUDYING);
        student.setGuardianName(req.guardianName());
        student.setGuardianPhone(req.guardianPhone());
        student.setCreatedBy(ctx.performedBy());

        Student saved = studentRepository.save(student);

        auditLogService.log("students", saved.getStudentId(), AuditAction.INSERT,
                null, snapshotOf(saved), null, ctx);

        return StudentResponse.from(saved);
    }

    /**
     * Updates student fields.
     * @param isStaff  true = staff/admin can update any field;
     *                 false = student can only update address, phone, email.
     */
    @Transactional
    public StudentResponse update(String studentId, UpdateStudentRequest req,
                                   boolean isStaff, AuditContext ctx) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));

        Map<String, Object> oldSnapshot = snapshotOf(student);
        List<String> changed = new ArrayList<>();

        // Fields editable by student (own profile only)
        if (req.email() != null) {
            if (!req.email().equalsIgnoreCase(student.getEmail()) &&
                    studentRepository.existsByEmail(req.email().toLowerCase())) {
                throw new AppException("EMAIL_CONFLICT", "เธญเธตเน€เธกเธฅเธเธตเนเธกเธตเนเธเธฃเธฐเธเธเนเธฅเนเธง", HttpStatus.CONFLICT);
            }
            student.setEmail(req.email().toLowerCase());
            changed.add("email");
        }
        if (req.phone() != null)   { student.setPhone(req.phone());     changed.add("phone"); }
        if (req.address() != null) { student.setAddress(req.address()); changed.add("address"); }

        // Fields editable by staff/admin only
        if (isStaff) {
            if (req.titleTh() != null)      { student.setTitleTh(req.titleTh());           changed.add("title_th"); }
            if (req.firstNameTh() != null)  { student.setFirstNameTh(req.firstNameTh());   changed.add("first_name_th"); }
            if (req.lastNameTh() != null)   { student.setLastNameTh(req.lastNameTh());     changed.add("last_name_th"); }
            if (req.firstNameEn() != null)  { student.setFirstNameEn(req.firstNameEn());   changed.add("first_name_en"); }
            if (req.lastNameEn() != null)   { student.setLastNameEn(req.lastNameEn());     changed.add("last_name_en"); }
            if (req.dateOfBirth() != null)  { student.setDateOfBirth(req.dateOfBirth());   changed.add("date_of_birth"); }
            if (req.gender() != null)       { student.setGender(req.gender());             changed.add("gender"); }
            if (req.nationality() != null)  { student.setNationality(req.nationality());   changed.add("nationality"); }
            if (req.guardianName() != null) { student.setGuardianName(req.guardianName()); changed.add("guardian_name"); }
            if (req.guardianPhone() != null){ student.setGuardianPhone(req.guardianPhone()); changed.add("guardian_phone"); }
            if (req.curriculumId() != null) {
                Curriculum curriculum = curriculumRepository.findById(req.curriculumId())
                        .orElseThrow(() -> new AppException("CURRICULUM_NOT_FOUND",
                                "Curriculum not found.", HttpStatus.UNPROCESSABLE_ENTITY));
                student.setCurriculum(curriculum);
                changed.add("curriculum_id");
            }
        }

        student.setUpdatedBy(ctx.performedBy());
        Student saved = studentRepository.save(student);

        auditLogService.log("students", saved.getStudentId(), AuditAction.UPDATE,
                oldSnapshot, snapshotOf(saved), changed, ctx);

        return StudentResponse.from(saved);
    }

    /** Changes student status (Staff/Admin only). */
    @Transactional
    public StudentResponse updateStatus(String studentId, UpdateStudentStatusRequest req, AuditContext ctx) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));

        Map<String, Object> oldSnapshot = snapshotOf(student);
        student.setStudentStatus(req.studentStatus());
        student.setUpdatedBy(ctx.performedBy());

        AuditContext auditCtx = ctx.withReason(req.reason(), req.documentRef());
        Student saved = studentRepository.save(student);

        auditLogService.log("students", saved.getStudentId(), AuditAction.UPDATE,
                oldSnapshot, snapshotOf(saved), List.of("student_status"), auditCtx);

        return StudentResponse.from(saved);
    }

    /** Updates the photo URL for a student. */
    @Transactional
    public StudentResponse updatePhoto(String studentId, String photoUrl, AuditContext ctx) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));
        student.setPhotoUrl(photoUrl);
        student.setUpdatedBy(ctx.performedBy());
        Student saved = studentRepository.save(student);
        auditLogService.log("students", saved.getStudentId(), AuditAction.UPDATE,
                Map.of("photo_url", ""), Map.of("photo_url", photoUrl), List.of("photo_url"), ctx);
        return StudentResponse.from(saved);
    }

    /**
     * Generates student_id: {YY}{CC:02d}{SSSS:04d}
     * e.g. admission 2567, curriculum 1 โ’ 67010001
     */
    private String generateStudentId(int admissionYear, Long curriculumId) {
        int seq = studentRepository.countByAdmissionYearAndCurriculumId(admissionYear, curriculumId) + 1;
        return String.format("%02d%02d%04d", admissionYear % 100, curriculumId % 100, seq);
    }

    private Map<String, Object> snapshotOf(Student s) {
        Map<String, Object> m = new HashMap<>();
        m.put("student_id", s.getStudentId());
        m.put("student_status", s.getStudentStatus().name());
        m.put("email", s.getEmail());
        m.put("phone", s.getPhone());
        m.put("address", s.getAddress());
        m.put("photo_url", s.getPhotoUrl());
        return m;
    }
}

