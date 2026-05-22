CREATE TABLE faculties (
    id          BIGSERIAL    PRIMARY KEY,
    code        VARCHAR(20)  NOT NULL UNIQUE,
    name_th     VARCHAR(200) NOT NULL,
    name_en     VARCHAR(200) NOT NULL,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE departments (
    id          BIGSERIAL    PRIMARY KEY,
    faculty_id  BIGINT       NOT NULL REFERENCES faculties(id),
    code        VARCHAR(20)  NOT NULL UNIQUE,
    name_th     VARCHAR(200) NOT NULL,
    name_en     VARCHAR(200) NOT NULL,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_departments_faculty_id ON departments(faculty_id);
