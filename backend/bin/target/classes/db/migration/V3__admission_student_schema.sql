-- ============================================================
-- V3: Admission workflow + Student lifecycle
-- ============================================================

CREATE TABLE admission_applications (
    id                      BIGSERIAL PRIMARY KEY,
    admission_ref_no        VARCHAR(30) NOT NULL UNIQUE,
    full_name               VARCHAR(150) NOT NULL,
    gender                  VARCHAR(10) NOT NULL CHECK (gender IN ('MALE','FEMALE','OTHER')),
    date_of_birth            DATE NOT NULL,
    mobile_number            VARCHAR(20) NOT NULL,
    email                    VARCHAR(150),
    address                  TEXT,
    category                 VARCHAR(30),
    id_proof_number          VARCHAR(50),
    course_id                BIGINT NOT NULL REFERENCES courses(id),
    academic_year_id         BIGINT NOT NULL REFERENCES academic_years(id),
    entry_semester           INT NOT NULL DEFAULT 1,
    status                   VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED'
                                CHECK (status IN ('SUBMITTED','UNDER_REVIEW','APPROVED','REJECTED','STUDENT_CREATED')),
    rejection_remarks        TEXT,
    reviewed_by              BIGINT REFERENCES users(id),
    reviewed_at              TIMESTAMPTZ,
    submitted_by             BIGINT REFERENCES users(id),
    created_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at                TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE admission_documents (
    id              BIGSERIAL PRIMARY KEY,
    application_id  BIGINT NOT NULL REFERENCES admission_applications(id) ON DELETE CASCADE,
    document_type   VARCHAR(50) NOT NULL,  -- PHOTO, SIGNATURE, MARKSHEET, TC, CASTE_CERT, INCOME_CERT, ID_PROOF, OTHER
    file_name        VARCHAR(255) NOT NULL,
    file_path        VARCHAR(500) NOT NULL,
    content_type     VARCHAR(100),
    file_size_bytes  BIGINT,
    uploaded_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE admission_guardians (
    id                  BIGSERIAL PRIMARY KEY,
    application_id      BIGINT NOT NULL UNIQUE REFERENCES admission_applications(id) ON DELETE CASCADE,
    father_name          VARCHAR(150),
    mother_name          VARCHAR(150),
    guardian_name        VARCHAR(150),
    guardian_contact     VARCHAR(20),
    occupation           VARCHAR(100),
    annual_income        NUMERIC(12,2)
);

CREATE TABLE admission_academics (
    id                      BIGSERIAL PRIMARY KEY,
    application_id           BIGINT NOT NULL UNIQUE REFERENCES admission_applications(id) ON DELETE CASCADE,
    previous_institution     VARCHAR(255),
    qualification            VARCHAR(100),
    board_university         VARCHAR(150),
    year_of_passing          INT,
    marks_percentage         NUMERIC(5,2)
);

CREATE TABLE admission_status_history (
    id              BIGSERIAL PRIMARY KEY,
    application_id  BIGINT NOT NULL REFERENCES admission_applications(id) ON DELETE CASCADE,
    from_status     VARCHAR(20),
    to_status       VARCHAR(20) NOT NULL,
    remarks         TEXT,
    changed_by       BIGINT REFERENCES users(id),
    changed_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============================================================
-- Students
-- ============================================================

CREATE TABLE students (
    id                      BIGSERIAL PRIMARY KEY,
    student_code             VARCHAR(30) NOT NULL UNIQUE,
    admission_application_id BIGINT UNIQUE REFERENCES admission_applications(id),
    user_id                  BIGINT UNIQUE REFERENCES users(id),
    full_name                VARCHAR(150) NOT NULL,
    gender                   VARCHAR(10) NOT NULL CHECK (gender IN ('MALE','FEMALE','OTHER')),
    date_of_birth             DATE NOT NULL,
    mobile_number             VARCHAR(20),
    email                     VARCHAR(150),
    address                   TEXT,
    category                  VARCHAR(30),
    course_id                 BIGINT NOT NULL REFERENCES courses(id),
    department_id             BIGINT NOT NULL REFERENCES departments(id),
    current_semester          INT NOT NULL DEFAULT 1,
    academic_year_id          BIGINT NOT NULL REFERENCES academic_years(id),
    status                    VARCHAR(20) NOT NULL DEFAULT 'ADMITTED'
                                CHECK (status IN ('ADMITTED','ACTIVE','SUSPENDED','GRADUATED','WITHDRAWN')),
    admitted_on               DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at                TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at                TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted                   BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE student_guardians (
    id              BIGSERIAL PRIMARY KEY,
    student_id      BIGINT NOT NULL UNIQUE REFERENCES students(id) ON DELETE CASCADE,
    father_name      VARCHAR(150),
    mother_name      VARCHAR(150),
    guardian_name    VARCHAR(150),
    guardian_contact VARCHAR(20),
    occupation       VARCHAR(100),
    annual_income    NUMERIC(12,2)
);

CREATE TABLE student_academics (
    id                      BIGSERIAL PRIMARY KEY,
    student_id               BIGINT NOT NULL UNIQUE REFERENCES students(id) ON DELETE CASCADE,
    previous_institution     VARCHAR(255),
    qualification            VARCHAR(100),
    board_university         VARCHAR(150),
    year_of_passing          INT,
    marks_percentage         NUMERIC(5,2)
);

CREATE TABLE student_status_history (
    id          BIGSERIAL PRIMARY KEY,
    student_id  BIGINT NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    from_status VARCHAR(20),
    to_status   VARCHAR(20) NOT NULL,
    remarks     TEXT,
    changed_by  BIGINT REFERENCES users(id),
    changed_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Which class/section a student belongs to for a given academic year + semester
CREATE TABLE student_class_enrollments (
    id                  BIGSERIAL PRIMARY KEY,
    student_id           BIGINT NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    class_section_id     BIGINT NOT NULL REFERENCES class_sections(id) ON DELETE CASCADE,
    semester             INT NOT NULL,
    enrolled_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (student_id, class_section_id, semester)
);

CREATE INDEX idx_admission_status ON admission_applications(status);
CREATE INDEX idx_students_course ON students(course_id);
CREATE INDEX idx_students_status ON students(status);
CREATE INDEX idx_enrollments_section ON student_class_enrollments(class_section_id, semester);
