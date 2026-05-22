CREATE TYPE gender_type    AS ENUM ('MALE', 'FEMALE', 'OTHER');
CREATE TYPE student_status AS ENUM ('STUDYING', 'LEAVE', 'RESIGNED', 'GRADUATED', 'EXPELLED');

CREATE TABLE students (
    student_id      VARCHAR(15)     PRIMARY KEY,
    national_id     VARCHAR(13)     NOT NULL UNIQUE,
    title_th        VARCHAR(20)     NOT NULL,
    first_name_th   VARCHAR(100)    NOT NULL,
    last_name_th    VARCHAR(100)    NOT NULL,
    first_name_en   VARCHAR(100)    NOT NULL,
    last_name_en    VARCHAR(100)    NOT NULL,
    date_of_birth   DATE            NOT NULL,
    gender          gender_type     NOT NULL,
    nationality     VARCHAR(50)     NOT NULL,
    email           VARCHAR(150)    NOT NULL UNIQUE,
    phone           VARCHAR(20)     NOT NULL,
    address         TEXT            NOT NULL,
    curriculum_id   BIGINT          NOT NULL REFERENCES curricula(id),
    admission_year  INT             NOT NULL,
    student_status  student_status  NOT NULL DEFAULT 'STUDYING',
    guardian_name   VARCHAR(200),
    guardian_phone  VARCHAR(20),
    photo_url       VARCHAR(500),
    created_by      VARCHAR(100)    NOT NULL,
    updated_by      VARCHAR(100),
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);

CREATE INDEX idx_students_national_id     ON students(national_id);
CREATE INDEX idx_students_email           ON students(email);
CREATE INDEX idx_students_curriculum_id   ON students(curriculum_id);
CREATE INDEX idx_students_admission_year  ON students(admission_year);
CREATE INDEX idx_students_status          ON students(student_status) WHERE deleted_at IS NULL;
CREATE INDEX idx_students_name_th         ON students(last_name_th, first_name_th) WHERE deleted_at IS NULL;
