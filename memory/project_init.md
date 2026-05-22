---
name: project-init
description: Full project setup for unext-backend — Spring Boot 3.3 / Java 21, modules, response format, auth
metadata:
  type: project
---

Project `unext-backend` initialized 2026-05-22 from CLAUDE.md SOP + ProgramSpec v1.0.

**Stack:** Spring Boot 3.3, Java 21, PostgreSQL 16, Flyway, JPA/Hibernate, JJWT 0.12, springdoc

**Base URL:** `http://localhost:8080/api` (server.servlet.context-path=/api)

**JSON format:** Jackson SNAKE_CASE globally. Response envelope matches spec:
`{ "status": "success"|"error", "code": 200, "message": "...", "data": {...}, "errors": [...] }`

**Package root:** `com.unext.backend`

**Modules implemented:**
- `auth/` — POST /v1/auth/login, /refresh-token, /logout (JWT + refresh token rotation)
- `user/` — CRUD /v1/users (admin only)
- `faculty/` — GET /v1/faculties, /v1/faculties/{id}/departments (lookup)
- `curriculum/` — Full CRUD /v1/curricula (PGM-001); curriculum_code auto-gen: `DEPT-YEAR-SEQ`
- `student/` — Full CRUD /v1/students (PGM-002); student_id auto-gen: `YYccSSSS`
- `audit/` — AuditLogService (REQUIRES_NEW transaction), stored in `audit_logs` table with JSONB

**Roles:** ADMIN, STAFF (registrar), STUDENT, USER, MODERATOR (enum: UserRole)

**Flyway migrations:** V1 users → V2 refresh_tokens → V3 add STAFF/STUDENT roles →
V4 faculties+departments → V5 curricula → V6 students → V7 audit_logs → V8 seed data

**Key design decisions:**
- Audit logging at application layer (not DB triggers) via AuditLogService with REQUIRES_NEW propagation
- Students can only edit own address/phone/email; staff can edit all — enforced in StudentService
- curriculum_code uniqueness guaranteed via loop check after code generation
- student_id format: 2-digit year + 2-digit curriculum + 4-digit sequence

**Why:** Implemented from spec doc `Claude_U-Next_ProgramSpec_v1.0 (1).docx`

**How to apply:** When adding new modules, follow the vertical slice pattern under new package. Always pass AuditContext from controller → service.
