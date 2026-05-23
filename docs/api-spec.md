# UNext Backend — API Specification

> **Base URL:** `http://localhost:8080/api`  
> **Version:** v1  
> **Content-Type:** `application/json` (all requests and responses)  
> **Last Updated:** May 2026

---

## Table of Contents

1. [Authentication & Response Format](#1-authentication--response-format)
2. [Auth Endpoints](#2-auth-endpoints)
3. [User Management](#3-user-management)
4. [Faculty & Department Lookup](#4-faculty--department-lookup)
5. [Curriculum Management](#5-curriculum-management)
6. [Student Management](#6-student-management)
7. [Enums Reference](#7-enums-reference)
8. [Error Codes Reference](#8-error-codes-reference)

---

## 1. Authentication & Response Format

### 1.1 Authentication

All endpoints **except** `/v1/auth/login` and `/v1/auth/refresh-token` require a Bearer token.

```
Authorization: Bearer <access_token>
```

Access tokens expire in **15 minutes**. Use the refresh endpoint to obtain a new one.

---

### 1.2 Standard Response Envelope

Every response wraps data in a consistent envelope:

```json
{
  "status": "success" | "error",
  "code": 200,
  "message": "Human-readable message",
  "data": { ... },
  "errors": null
}
```

| Field | Type | Notes |
|-------|------|-------|
| `status` | `string` | `"success"` or `"error"` |
| `code` | `integer` | HTTP status code mirrored |
| `message` | `string` | Omitted when `null` |
| `data` | `object \| array \| null` | Payload; omitted when `null` |
| `errors` | `FieldError[]` | Only present on 422 responses |

**FieldError (422 only):**
```json
{
  "errors": [
    { "field": "email", "message": "must be a well-formed email address" }
  ]
}
```

---

### 1.3 Paginated Response

Endpoints returning lists wrap items in a `PagedData` structure:

```json
{
  "status": "success",
  "code": 200,
  "data": {
    "items": [ ... ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total": 150,
      "total_pages": 8
    }
  }
}
```

---

## 2. Auth Endpoints

### POST `/v1/auth/login`

Login with email and password. No authentication header required.

**Request Body:**
```json
{
  "email": "staff@university.ac.th",
  "password": "P@ssw0rd123"
}
```

| Field | Type | Required | Validation |
|-------|------|----------|------------|
| `email` | `string` | Yes | Valid email format |
| `password` | `string` | Yes | Not blank |

**Response `200`:**
```json
{
  "status": "success",
  "code": 200,
  "message": "Login successful",
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiJ9...",
    "refresh_token": "550e8400-e29b-41d4-a716-446655440000",
    "token_type": "Bearer",
    "expires_in": 900
  }
}
```

| Field | Type | Notes |
|-------|------|-------|
| `access_token` | `string` | JWT; expires in `expires_in` seconds |
| `refresh_token` | `string` | UUID; expires in 7 days |
| `token_type` | `string` | Always `"Bearer"` |
| `expires_in` | `integer` | Seconds until access token expiry (default 900) |

**Error Responses:**

| Status | Condition |
|--------|-----------|
| `401` | Invalid email or password |
| `422` | Validation error (blank email/password) |

---

### POST `/v1/auth/refresh-token`

Exchange a valid refresh token for a new access token. No authentication header required.

**Request Body:**
```json
{
  "refresh_token": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response `200`:** Same structure as Login `200`.

**Error Responses:**

| Status | Condition |
|--------|-----------|
| `401` | Refresh token is expired, already used, or not found |

---

### POST `/v1/auth/logout`

Revoke all refresh tokens for the authenticated user. Requires Bearer token.

**Request Body:** None

**Response `204`:** No content.

---

## 3. User Management

> **Required Role:** `ADMIN` for all endpoints.

---

### POST `/v1/users`

Create a new user account.

**Request Body:**
```json
{
  "email": "newuser@example.com",
  "password": "P@ssw0rd123",
  "role": "STAFF"
}
```

| Field | Type | Required | Validation |
|-------|------|----------|------------|
| `email` | `string` | Yes | Valid email format |
| `password` | `string` | Yes | 8–100 characters |
| `role` | `UserRole` | Yes | See [Enums](#7-enums-reference) |

**Response `201`:**
```json
{
  "status": "success",
  "code": 201,
  "message": "User created successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "newuser@example.com",
    "role": "STAFF",
    "active": true,
    "created_at": "2026-05-01T10:00:00Z",
    "updated_at": "2026-05-01T10:00:00Z"
  }
}
```

**Error Responses:**

| Status | Condition |
|--------|-----------|
| `403` | Caller is not ADMIN |
| `409` | Email already registered |
| `422` | Validation error |

---

### GET `/v1/users`

List all active users with pagination.

**Query Parameters:**

| Param | Type | Default | Validation |
|-------|------|---------|------------|
| `page` | `integer` | `1` | ≥ 1 |
| `limit` | `integer` | `20` | 1–100 |

**Response `200`:**
```json
{
  "status": "success",
  "code": 200,
  "data": {
    "items": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "email": "user@example.com",
        "role": "STAFF",
        "active": true,
        "created_at": "2026-05-01T10:00:00Z",
        "updated_at": "2026-05-01T10:00:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total": 5,
      "total_pages": 1
    }
  }
}
```

---

### GET `/v1/users/{id}`

Get a user by UUID.

> **Access:** ADMIN — or the user themselves (own profile).

**Path Parameter:** `id` — UUID

**Response `200`:** Single `UserResponse` object in `data`.

**Error Responses:**

| Status | Condition |
|--------|-----------|
| `404` | User not found |

---

### PATCH `/v1/users/{id}`

Update a user's password, role, or active status. All fields optional.

> **Access:** ADMIN — or the user themselves (own profile).

**Request Body:**
```json
{
  "password": "NewP@ss123",
  "role": "ADMIN",
  "active": false
}
```

| Field | Type | Required | Validation |
|-------|------|----------|------------|
| `password` | `string` | No | 8–100 characters |
| `role` | `UserRole` | No | See [Enums](#7-enums-reference) |
| `active` | `boolean` | No | — |

**Response `200`:** Updated `UserResponse` in `data`.

---

### DELETE `/v1/users/{id}`

Delete a user account permanently.

**Response `204`:** No content.

**Error Responses:**

| Status | Condition |
|--------|-----------|
| `404` | User not found |

---

## 4. Faculty & Department Lookup

> **Required Role:** Any authenticated user.

---

### GET `/v1/faculties`

List all active faculties.

**Response `200`:**
```json
{
  "status": "success",
  "code": 200,
  "data": [
    {
      "id": 1,
      "code": "ENG",
      "name_th": "คณะวิศวกรรมศาสตร์",
      "name_en": "Faculty of Engineering"
    }
  ]
}
```

---

### GET `/v1/faculties/{id}/departments`

List departments belonging to a faculty.

**Path Parameter:** `id` — Faculty ID (long)

**Response `200`:**
```json
{
  "status": "success",
  "code": 200,
  "data": [
    {
      "id": 10,
      "faculty_id": 1,
      "code": "CS",
      "name_th": "สาขาวิทยาการคอมพิวเตอร์",
      "name_en": "Computer Science"
    }
  ]
}
```

**Error Responses:**

| Status | Condition |
|--------|-----------|
| `404` | Faculty not found |

---

## 5. Curriculum Management

> **Required Role:** `ADMIN` or `STAFF` (unless noted).

---

### GET `/v1/curricula`

Search and filter curricula with pagination.

**Query Parameters:**

| Param | Type | Required | Notes |
|-------|------|----------|-------|
| `degreeLevel` | `string` | No | e.g. `"Bachelor"`, `"Master"` |
| `status` | `string` | No | `ACTIVE`, `INACTIVE`, `DRAFT` |
| `facultyId` | `long` | No | Filter by faculty |
| `keyword` | `string` | No | Full-text search on name |
| `page` | `integer` | No | Default `1` |
| `limit` | `integer` | No | Default `20`, max `100` |

**Response `200`:**
```json
{
  "status": "success",
  "code": 200,
  "data": {
    "items": [
      {
        "curriculum_id": 1,
        "curriculum_code": "CS-2566-01",
        "curriculum_name_th": "หลักสูตรวิทยาการคอมพิวเตอร์",
        "curriculum_name_en": "Computer Science",
        "degree_level": "Bachelor",
        "faculty_id": 1,
        "faculty_name": "คณะวิศวกรรมศาสตร์",
        "department_id": 10,
        "department_name": "สาขาวิทยาการคอมพิวเตอร์",
        "total_credits": 120,
        "duration_years": "4.0",
        "effective_year": 2566,
        "accreditation_body": "ABET",
        "status": "ACTIVE",
        "description": "...",
        "created_by": "admin",
        "updated_by": null,
        "created_at": "2026-01-01T00:00:00Z",
        "updated_at": "2026-01-01T00:00:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total": 3,
      "total_pages": 1
    }
  }
}
```

---

### GET `/v1/curricula/{id}`

Get a curriculum by ID.

**Path Parameter:** `id` — Curriculum ID (long)

**Response `200`:** Single `CurriculumResponse` in `data`.

**Error Responses:**

| Status | Condition |
|--------|-----------|
| `404` | Curriculum not found |

---

### POST `/v1/curricula`

Create a new curriculum.

**Request Body:**
```json
{
  "curriculum_name_th": "หลักสูตรวิทยาการคอมพิวเตอร์",
  "curriculum_name_en": "Computer Science",
  "degree_level": "Bachelor",
  "faculty_id": 1,
  "department_id": 10,
  "total_credits": 120,
  "duration_years": "4.0",
  "effective_year": 2567,
  "accreditation_body": "ABET",
  "status": "DRAFT",
  "description": "Optional description"
}
```

| Field | Type | Required | Validation |
|-------|------|----------|------------|
| `curriculum_name_th` | `string` | Yes | max 200 chars |
| `curriculum_name_en` | `string` | Yes | max 200 chars |
| `degree_level` | `string` | Yes | max 50 chars |
| `faculty_id` | `long` | Yes | Must exist |
| `department_id` | `long` | Yes | Must exist |
| `total_credits` | `integer` | Yes | 60–180 |
| `duration_years` | `decimal` | Yes | 1.0–8.0 |
| `effective_year` | `integer` | Yes | ≥ 2500 (Buddhist Era) |
| `accreditation_body` | `string` | No | max 100 chars |
| `status` | `CurriculumStatus` | No | Default: `DRAFT` |
| `description` | `string` | No | — |

**Response `201`:** Created `CurriculumResponse` in `data`.  
**Location Header:** `/v1/curricula/{id}`

**Error Responses:**

| Status | Condition |
|--------|-----------|
| `404` | Faculty or Department not found |
| `422` | Validation error |

---

### PUT `/v1/curricula/{id}`

Update an existing curriculum. All fields optional — only provided fields are updated.

**Request Body:**
```json
{
  "curriculum_name_th": "ชื่อใหม่",
  "status": "ACTIVE",
  "total_credits": 130
}
```

| Field | Type | Validation |
|-------|------|------------|
| `curriculum_name_th` | `string` | max 200 chars |
| `curriculum_name_en` | `string` | max 200 chars |
| `degree_level` | `string` | max 50 chars |
| `faculty_id` | `long` | Must exist |
| `department_id` | `long` | Must exist |
| `total_credits` | `integer` | 60–180 |
| `duration_years` | `decimal` | 1.0–8.0 |
| `effective_year` | `integer` | ≥ 2500 |
| `accreditation_body` | `string` | max 100 chars |
| `status` | `CurriculumStatus` | — |
| `description` | `string` | — |

**Response `200`:** Updated `CurriculumResponse` in `data`.

**Error Responses:**

| Status | Condition |
|--------|-----------|
| `404` | Curriculum, Faculty, or Department not found |
| `422` | Validation error |

---

### DELETE `/v1/curricula/{id}`

Soft-delete a curriculum (sets status to `INACTIVE`). The record is not removed from the database.

**Response `200`:**
```json
{
  "status": "success",
  "code": 200,
  "message": "Curriculum deactivated successfully"
}
```

**Error Responses:**

| Status | Condition |
|--------|-----------|
| `404` | Curriculum not found |

---

### GET `/v1/curricula/{id}/audit`

View the full audit history for a curriculum.

> **Required Role:** `ADMIN` only.

**Query Parameters:** `page` (default 1), `limit` (default 20, max 100)

**Response `200`:**
```json
{
  "status": "success",
  "code": 200,
  "data": {
    "items": [
      {
        "audit_id": 42,
        "table_name": "curricula",
        "record_id": "1",
        "action": "UPDATE",
        "old_values": { "status": "DRAFT" },
        "new_values": { "status": "ACTIVE" },
        "changed_fields": ["status"],
        "performed_by": "admin-uuid",
        "performed_by_name": "Admin User",
        "user_role": "ADMIN",
        "ip_address": "192.168.1.1",
        "reason": null,
        "document_ref": null,
        "performed_at": "2026-05-01T10:30:00Z"
      }
    ],
    "pagination": { "page": 1, "limit": 20, "total": 1, "total_pages": 1 }
  }
}
```

---

## 6. Student Management

> **Required Role:** `ADMIN` or `STAFF` (unless noted). Students can access their own records on selected endpoints.

---

### GET `/v1/students`

Search students with filters and pagination.

**Query Parameters:**

| Param | Type | Required | Notes |
|-------|------|----------|-------|
| `keyword` | `string` | No | Searches name, student ID, national ID |
| `status` | `string` | No | See `StudentStatus` enum |
| `curriculumId` | `long` | No | Filter by curriculum |
| `admissionYear` | `integer` | No | Buddhist Era year e.g. `2567` |
| `page` | `integer` | No | Default `1` |
| `limit` | `integer` | No | Default `20`, max `100` |

**Response `200`:**
```json
{
  "status": "success",
  "code": 200,
  "data": {
    "items": [
      {
        "student_id": "6701000001",
        "national_id": "1234567890123",
        "title_th": "นาย",
        "first_name_th": "สมชาย",
        "last_name_th": "ทดสอบ",
        "first_name_en": "Somchai",
        "last_name_en": "Test",
        "date_of_birth": "2000-01-15",
        "gender": "MALE",
        "nationality": "Thai",
        "email": "student@example.com",
        "phone": "0812345678",
        "address": "123 Test St",
        "curriculum": {
          "curriculum_id": 1,
          "curriculum_code": "CS-2566-01",
          "curriculum_name_th": "วิทยาการคอมพิวเตอร์"
        },
        "admission_year": 2567,
        "student_status": "STUDYING",
        "guardian_name": null,
        "guardian_phone": null,
        "photo_url": null,
        "created_at": "2026-01-01T00:00:00Z",
        "updated_at": "2026-01-01T00:00:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total": 1,
      "total_pages": 1
    }
  }
}
```

---

### GET `/v1/students/{studentId}`

Get a student by student ID.

> **Access:** ADMIN/STAFF — or the student themselves.

**Path Parameter:** `studentId` — Student ID string (e.g. `"6701000001"`)

**Response `200`:** Single `StudentResponse` in `data`.

**Error Responses:**

| Status | Condition |
|--------|-----------|
| `404` | Student not found |

---

### POST `/v1/students`

Register a new student.

**Request Body:**
```json
{
  "national_id": "1234567890123",
  "title_th": "นาย",
  "first_name_th": "สมชาย",
  "last_name_th": "ทดสอบ",
  "first_name_en": "Somchai",
  "last_name_en": "Test",
  "date_of_birth": "2000-01-15",
  "gender": "MALE",
  "nationality": "Thai",
  "email": "student@example.com",
  "phone": "0812345678",
  "address": "123 Test St",
  "curriculum_id": 1,
  "admission_year": 2567,
  "guardian_name": "สมศรี ทดสอบ",
  "guardian_phone": "0898765432"
}
```

| Field | Type | Required | Validation |
|-------|------|----------|------------|
| `national_id` | `string` | Yes | Exactly 13 digits |
| `title_th` | `string` | Yes | max 20 chars |
| `first_name_th` | `string` | Yes | max 100 chars |
| `last_name_th` | `string` | Yes | max 100 chars |
| `first_name_en` | `string` | Yes | max 100 chars |
| `last_name_en` | `string` | Yes | max 100 chars |
| `date_of_birth` | `date` | Yes | ISO 8601 `YYYY-MM-DD`, must be in the past |
| `gender` | `Gender` | Yes | `MALE`, `FEMALE`, `OTHER` |
| `nationality` | `string` | Yes | max 50 chars |
| `email` | `string` | Yes | Valid email, max 150 chars |
| `phone` | `string` | Yes | max 20 chars |
| `address` | `string` | Yes | — |
| `curriculum_id` | `long` | Yes | Must exist |
| `admission_year` | `integer` | Yes | ≥ 2500 (Buddhist Era) |
| `guardian_name` | `string` | No | max 200 chars |
| `guardian_phone` | `string` | No | max 20 chars |

**Response `201`:** Created `StudentResponse` in `data`.  
**Location Header:** `/v1/students/{studentId}`

**Error Responses:**

| Status | Condition |
|--------|-----------|
| `404` | Curriculum not found |
| `409` | National ID or email already registered |
| `422` | Validation error |

---

### PATCH `/v1/students/{studentId}`

Update student information. All fields optional.

> **Access:** ADMIN/STAFF can update all fields. Students can only update `email`, `phone`, and `address`.

**Request Body:**
```json
{
  "email": "newemail@example.com",
  "phone": "0899999999",
  "address": "456 New Address",
  "title_th": "นาย",
  "first_name_th": "ชื่อใหม่",
  "last_name_th": "นามสกุลใหม่",
  "first_name_en": "NewName",
  "last_name_en": "NewSurname",
  "date_of_birth": "2000-06-01",
  "gender": "MALE",
  "nationality": "Thai",
  "curriculum_id": 2,
  "guardian_name": "ผู้ปกครอง",
  "guardian_phone": "0811111111"
}
```

**Response `200`:** Updated `StudentResponse` in `data`.

---

### PATCH `/v1/students/{studentId}/status`

Change a student's status (e.g. graduate, resign, take leave).

> **Required Role:** `ADMIN` or `STAFF`.

**Request Body:**
```json
{
  "student_status": "GRADUATED",
  "effective_date": "2026-05-20",
  "reason": "สำเร็จการศึกษา",
  "document_ref": "DOC-2026-001"
}
```

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `student_status` | `StudentStatus` | Yes | See [Enums](#7-enums-reference) |
| `effective_date` | `date` | No | ISO 8601 `YYYY-MM-DD` |
| `reason` | `string` | No | Recorded in audit log |
| `document_ref` | `string` | No | Reference document number |

**Response `200`:** Updated `StudentResponse` in `data`.

**Error Responses:**

| Status | Condition |
|--------|-----------|
| `404` | Student not found |

---

### PATCH `/v1/students/{studentId}/photo`

Update the student's photo URL.

> **Access:** ADMIN/STAFF — or the student themselves.

**Request Body:**
```json
{
  "photo_url": "https://cdn.example.com/photos/student-001.jpg"
}
```

| Field | Type | Required | Validation |
|-------|------|----------|------------|
| `photo_url` | `string` | Yes | Not blank |

**Response `200`:** Updated `StudentResponse` in `data`.

---

### GET `/v1/students/{studentId}/audit`

View the full audit history for a student record.

> **Required Role:** `ADMIN` or `STAFF`.

**Query Parameters:** `page` (default 1), `limit` (default 20, max 100)

**Response `200`:** Same structure as [Curriculum Audit](#get-v1curriculaidaudit).

---

## 7. Enums Reference

### UserRole

| Value | Description |
|-------|-------------|
| `ADMIN` | Full system access |
| `STAFF` | Academic staff — can manage students and curricula |
| `STUDENT` | Student — read-only access to own record |
| `MODERATOR` | Reserved for future use |
| `USER` | Generic user — limited access |

---

### CurriculumStatus

| Value | Description |
|-------|-------------|
| `DRAFT` | Under preparation, not yet published (default on create) |
| `ACTIVE` | Published and accepting students |
| `INACTIVE` | Archived / soft-deleted |

---

### StudentStatus

| Value | Description |
|-------|-------------|
| `STUDYING` | Currently enrolled |
| `LEAVE` | On approved leave of absence |
| `RESIGNED` | Voluntarily withdrew |
| `GRADUATED` | Completed the program |
| `EXPELLED` | Disciplinary removal |

---

### Gender

| Value |
|-------|
| `MALE` |
| `FEMALE` |
| `OTHER` |

---

### AuditAction

| Value | Trigger |
|-------|---------|
| `INSERT` | Record created |
| `UPDATE` | Record modified |
| `DELETE` | Record removed / deactivated |

---

## 8. Error Codes Reference

All error responses follow the standard envelope with `"status": "error"`.

| HTTP Status | When |
|-------------|------|
| `400` | Bad request — invalid query parameter values |
| `401` | Missing or expired/invalid Bearer token |
| `403` | Authenticated but insufficient role |
| `404` | Requested resource does not exist |
| `409` | Conflict — duplicate unique field (email, national ID) |
| `422` | Request body fails bean validation; `errors` array populated |
| `500` | Unexpected server error |

**Error response example:**
```json
{
  "status": "error",
  "code": 404,
  "message": "Curriculum with id '99' was not found."
}
```

**Validation error example (422):**
```json
{
  "status": "error",
  "code": 422,
  "message": "Validation failed",
  "errors": [
    { "field": "national_id", "message": "เลขบัตรประชาชนต้องมี 13 หลัก" },
    { "field": "email", "message": "รูปแบบอีเมลไม่ถูกต้อง" }
  ]
}
```

---

## Notes for Frontend Developers

- **Date format:** `date_of_birth` and `effective_date` are `YYYY-MM-DD` (ISO 8601). Timestamps (`created_at`, `updated_at`, `performed_at`) are ISO 8601 UTC strings.
- **Year values** (`admission_year`, `effective_year`) are **Buddhist Era (BE)** — subtract 543 to convert to CE (e.g. `2567` = 2024 CE).
- **Null fields** are omitted from JSON responses (not sent as `null`) due to `@JsonInclude(NON_NULL)`.
- **Pagination** is 1-indexed (`page=1` is the first page).
- **Soft delete:** `DELETE /v1/curricula/{id}` does not remove the record — it sets `status = INACTIVE`. The record may still appear in GET requests unless filtered.
- **Student ID** is auto-generated by the server on registration (format: `YYMMXXXXXX`). Do not send it in POST body.
- **CSRF:** Disabled on the API. No CSRF token is required for state-mutating requests.
- **CORS:** Configured on the server. Contact the backend team for allowed origins in production.
