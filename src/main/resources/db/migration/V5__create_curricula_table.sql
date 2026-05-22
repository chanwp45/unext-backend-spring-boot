CREATE TYPE curriculum_status AS ENUM ('ACTIVE', 'INACTIVE', 'DRAFT');

CREATE TABLE curricula (
    id                  BIGSERIAL        PRIMARY KEY,
    curriculum_code     VARCHAR(20)      NOT NULL UNIQUE,
    curriculum_name_th  VARCHAR(200)     NOT NULL,
    curriculum_name_en  VARCHAR(200)     NOT NULL,
    degree_level        VARCHAR(50)      NOT NULL,
    faculty_id          BIGINT           NOT NULL REFERENCES faculties(id),
    department_id       BIGINT           NOT NULL REFERENCES departments(id),
    total_credits       INT              NOT NULL CHECK (total_credits BETWEEN 60 AND 180),
    duration_years      DECIMAL(3,1)     NOT NULL,
    effective_year      INT              NOT NULL,
    accreditation_body  VARCHAR(100),
    status              curriculum_status NOT NULL DEFAULT 'DRAFT',
    description         TEXT,
    created_by          VARCHAR(100)     NOT NULL,
    updated_by          VARCHAR(100),
    created_at          TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);

CREATE INDEX idx_curricula_status          ON curricula(status) WHERE deleted_at IS NULL;
CREATE INDEX idx_curricula_degree_level    ON curricula(degree_level) WHERE deleted_at IS NULL;
CREATE INDEX idx_curricula_faculty_id      ON curricula(faculty_id);
CREATE INDEX idx_curricula_department_id   ON curricula(department_id);
CREATE INDEX idx_curricula_effective_year  ON curricula(effective_year);
