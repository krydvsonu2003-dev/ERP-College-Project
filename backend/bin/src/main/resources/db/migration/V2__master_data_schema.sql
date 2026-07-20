-- ============================================================
-- V2: Master data - departments, courses, academic structure
-- ============================================================

CREATE TABLE departments (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(150) NOT NULL UNIQUE,
    code        VARCHAR(20)  NOT NULL UNIQUE,
    hod_user_id BIGINT REFERENCES users(id),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE courses (
    id              BIGSERIAL PRIMARY KEY,
    department_id   BIGINT NOT NULL REFERENCES departments(id),
    name            VARCHAR(150) NOT NULL,
    code            VARCHAR(20)  NOT NULL UNIQUE,
    duration_years  NUMERIC(3,1) NOT NULL,
    total_semesters INT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE academic_years (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(20) NOT NULL UNIQUE,   -- e.g. 2025-26
    start_date  DATE NOT NULL,
    end_date    DATE NOT NULL,
    is_current  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE class_sections (
    id              BIGSERIAL PRIMARY KEY,
    course_id       BIGINT NOT NULL REFERENCES courses(id),
    academic_year_id BIGINT NOT NULL REFERENCES academic_years(id),
    semester        INT NOT NULL,
    section_name    VARCHAR(10) NOT NULL DEFAULT 'A',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (course_id, academic_year_id, semester, section_name)
);

CREATE TABLE subjects (
    id          BIGSERIAL PRIMARY KEY,
    course_id   BIGINT NOT NULL REFERENCES courses(id),
    semester    INT NOT NULL,
    name        VARCHAR(150) NOT NULL,
    code        VARCHAR(20)  NOT NULL UNIQUE,
    credits     NUMERIC(3,1) NOT NULL DEFAULT 4,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Faculty operational profile (a faculty is a `users` row with role FACULTY)
CREATE TABLE faculty_profiles (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    department_id   BIGINT REFERENCES departments(id),
    employee_code   VARCHAR(30) UNIQUE,
    designation     VARCHAR(100),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Which faculty teaches which subject for which class section (scopes access)
CREATE TABLE faculty_subject_assignments (
    id                  BIGSERIAL PRIMARY KEY,
    faculty_id          BIGINT NOT NULL REFERENCES faculty_profiles(id) ON DELETE CASCADE,
    subject_id          BIGINT NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    class_section_id    BIGINT NOT NULL REFERENCES class_sections(id) ON DELETE CASCADE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (faculty_id, subject_id, class_section_id)
);

CREATE INDEX idx_courses_department ON courses(department_id);
CREATE INDEX idx_subjects_course ON subjects(course_id, semester);
CREATE INDEX idx_class_sections_course_year ON class_sections(course_id, academic_year_id);
