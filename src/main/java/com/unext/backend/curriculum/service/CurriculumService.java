package com.unext.backend.curriculum.service;

import com.unext.backend.audit.entity.AuditAction;
import com.unext.backend.audit.service.AuditContext;
import com.unext.backend.audit.service.AuditLogService;
import com.unext.backend.curriculum.dto.CreateCurriculumRequest;
import com.unext.backend.curriculum.dto.CurriculumResponse;
import com.unext.backend.curriculum.dto.UpdateCurriculumRequest;
import com.unext.backend.curriculum.entity.Curriculum;
import com.unext.backend.curriculum.entity.CurriculumStatus;
import com.unext.backend.curriculum.exception.CurriculumNotFoundException;
import com.unext.backend.curriculum.repository.CurriculumRepository;
import com.unext.backend.faculty.entity.Department;
import com.unext.backend.faculty.entity.Faculty;
import com.unext.backend.faculty.repository.DepartmentRepository;
import com.unext.backend.faculty.repository.FacultyRepository;
import com.unext.backend.shared.exception.AppException;
import com.unext.backend.shared.response.PagedData;
import com.unext.backend.shared.response.PaginationMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CurriculumService {

    @Autowired private CurriculumRepository curriculumRepository;
    @Autowired private FacultyRepository facultyRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private AuditLogService auditLogService;

    /** Search/list curricula with optional filters and pagination. */
    @Transactional(readOnly = true)
    public PagedData<CurriculumResponse> search(String degreeLevel, String status,
                                                 Long facultyId, String keyword,
                                                 int page, int limit) {
        CurriculumStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = CurriculumStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new AppException("INVALID_STATUS",
                        "Invalid status value. Must be one of: ACTIVE, INACTIVE, DRAFT", HttpStatus.BAD_REQUEST);
            }
        }

        Page<Curriculum> result = curriculumRepository.search(
                degreeLevel, statusEnum, facultyId, keyword,
                PageRequest.of(page - 1, limit, Sort.by("createdAt").descending()));

        List<CurriculumResponse> items = result.getContent().stream().map(CurriculumResponse::from).toList();
        return new PagedData<>(items, PaginationMeta.of(page, limit, result.getTotalElements()));
    }

    /** Returns a single curriculum by ID. */
    @Transactional(readOnly = true)
    public CurriculumResponse findById(Long id) {
        return curriculumRepository.findById(id)
                .map(CurriculumResponse::from)
                .orElseThrow(() -> new CurriculumNotFoundException(id));
    }

    /** Creates a new curriculum and auto-generates curriculum_code. */
    @Transactional
    public CurriculumResponse create(CreateCurriculumRequest req, AuditContext ctx) {
        Faculty faculty = facultyRepository.findById(req.facultyId())
                .orElseThrow(() -> new AppException("FACULTY_NOT_FOUND",
                        "Faculty not found.", HttpStatus.UNPROCESSABLE_ENTITY));
        Department department = departmentRepository.findById(req.departmentId())
                .orElseThrow(() -> new AppException("DEPARTMENT_NOT_FOUND",
                        "Department not found.", HttpStatus.UNPROCESSABLE_ENTITY));

        Curriculum curriculum = new Curriculum();
        curriculum.setCurriculumCode(generateCode(department.getCode(), req.effectiveYear()));
        curriculum.setCurriculumNameTh(req.curriculumNameTh());
        curriculum.setCurriculumNameEn(req.curriculumNameEn());
        curriculum.setDegreeLevel(req.degreeLevel());
        curriculum.setFaculty(faculty);
        curriculum.setDepartment(department);
        curriculum.setTotalCredits(req.totalCredits());
        curriculum.setDurationYears(req.durationYears());
        curriculum.setEffectiveYear(req.effectiveYear());
        curriculum.setAccreditationBody(req.accreditationBody());
        curriculum.setStatus(req.status() != null ? req.status() : CurriculumStatus.DRAFT);
        curriculum.setDescription(req.description());
        curriculum.setCreatedBy(ctx.performedBy());

        Curriculum saved = curriculumRepository.save(curriculum);

        auditLogService.log("curricula", saved.getId().toString(), AuditAction.INSERT,
                null, snapshotOf(saved), null, ctx);

        return CurriculumResponse.from(saved);
    }

    /** Updates curriculum fields (only provided non-null values are changed). */
    @Transactional
    public CurriculumResponse update(Long id, UpdateCurriculumRequest req, AuditContext ctx) {
        Curriculum curriculum = curriculumRepository.findById(id)
                .orElseThrow(() -> new CurriculumNotFoundException(id));

        Map<String, Object> oldSnapshot = snapshotOf(curriculum);
        List<String> changed = new ArrayList<>();

        if (req.curriculumNameTh() != null) { curriculum.setCurriculumNameTh(req.curriculumNameTh()); changed.add("curriculum_name_th"); }
        if (req.curriculumNameEn() != null) { curriculum.setCurriculumNameEn(req.curriculumNameEn()); changed.add("curriculum_name_en"); }
        if (req.degreeLevel() != null)      { curriculum.setDegreeLevel(req.degreeLevel());           changed.add("degree_level"); }
        if (req.totalCredits() != null)     { curriculum.setTotalCredits(req.totalCredits());         changed.add("total_credits"); }
        if (req.durationYears() != null)    { curriculum.setDurationYears(req.durationYears());       changed.add("duration_years"); }
        if (req.effectiveYear() != null)    { curriculum.setEffectiveYear(req.effectiveYear());       changed.add("effective_year"); }
        if (req.accreditationBody() != null){ curriculum.setAccreditationBody(req.accreditationBody()); changed.add("accreditation_body"); }
        if (req.status() != null)           { curriculum.setStatus(req.status());                    changed.add("status"); }
        if (req.description() != null)      { curriculum.setDescription(req.description());          changed.add("description"); }
        if (req.facultyId() != null) {
            Faculty faculty = facultyRepository.findById(req.facultyId())
                    .orElseThrow(() -> new AppException("FACULTY_NOT_FOUND", "Faculty not found.", HttpStatus.UNPROCESSABLE_ENTITY));
            curriculum.setFaculty(faculty);
            changed.add("faculty_id");
        }
        if (req.departmentId() != null) {
            Department department = departmentRepository.findById(req.departmentId())
                    .orElseThrow(() -> new AppException("DEPARTMENT_NOT_FOUND", "Department not found.", HttpStatus.UNPROCESSABLE_ENTITY));
            curriculum.setDepartment(department);
            changed.add("department_id");
        }

        curriculum.setUpdatedBy(ctx.performedBy());
        Curriculum saved = curriculumRepository.save(curriculum);

        auditLogService.log("curricula", saved.getId().toString(), AuditAction.UPDATE,
                oldSnapshot, snapshotOf(saved), changed, ctx);

        return CurriculumResponse.from(saved);
    }

    /** Soft-deletes a curriculum by setting status to INACTIVE and deletedAt. */
    @Transactional
    public void softDelete(Long id, AuditContext ctx) {
        Curriculum curriculum = curriculumRepository.findById(id)
                .orElseThrow(() -> new CurriculumNotFoundException(id));

        Map<String, Object> oldSnapshot = snapshotOf(curriculum);
        curriculum.setStatus(CurriculumStatus.INACTIVE);
        curriculum.setDeletedAt(Instant.now());
        curriculum.setUpdatedBy(ctx.performedBy());
        curriculumRepository.save(curriculum);

        auditLogService.log("curricula", id.toString(), AuditAction.DELETE,
                oldSnapshot, null, null, ctx);
    }

    /** Generates unique curriculum code: DEPT_CODE-YEAR-SEQ (e.g. CS-2566-01). */
    private String generateCode(String deptCode, int effectiveYear) {
        int seq = curriculumRepository.countByCurriculumCodeStartingWithAndEffectiveYear(
                deptCode.toUpperCase() + "-" + effectiveYear, effectiveYear) + 1;
        String candidate = String.format("%s-%d-%02d", deptCode.toUpperCase(), effectiveYear, seq);
        while (curriculumRepository.existsByCurriculumCode(candidate)) {
            candidate = String.format("%s-%d-%02d", deptCode.toUpperCase(), effectiveYear, ++seq);
        }
        return candidate;
    }

    private Map<String, Object> snapshotOf(Curriculum c) {
        Map<String, Object> m = new HashMap<>();
        m.put("curriculum_id", c.getId());
        m.put("curriculum_code", c.getCurriculumCode());
        m.put("curriculum_name_th", c.getCurriculumNameTh());
        m.put("status", c.getStatus().name());
        m.put("total_credits", c.getTotalCredits());
        m.put("degree_level", c.getDegreeLevel());
        return m;
    }
}

