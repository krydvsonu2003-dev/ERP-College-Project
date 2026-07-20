-- ============================================================
-- V5: Examination & Result management
-- ============================================================

CREATE TABLE grade_master (
    id              BIGSERIAL PRIMARY KEY,
    grade_letter     VARCHAR(5) NOT NULL UNIQUE,   -- O, A+, A, B+, B, C, F ...
    min_percentage    NUMERIC(5,2) NOT NULL,
    max_percentage    NUMERIC(5,2) NOT NULL,
    grade_point       NUMERIC(3,1) NOT NULL,
    is_pass           BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE mark_components (
    id              BIGSERIAL PRIMARY KEY,
    code             VARCHAR(20) NOT NULL UNIQUE,  -- INTERNAL, PRACTICAL, VIVA, EXTERNAL
    name             VARCHAR(50) NOT NULL,
    weight_percentage NUMERIC(5,2) NOT NULL         -- default weight, overridable per exam_schedule
);

CREATE TABLE examinations (
    id                  BIGSERIAL PRIMARY KEY,
    name                 VARCHAR(150) NOT NULL,        -- e.g. "Semester 3 End-Term Examination"
    course_id             BIGINT NOT NULL REFERENCES courses(id),
    academic_year_id      BIGINT NOT NULL REFERENCES academic_years(id),
    semester              INT NOT NULL,
    exam_type             VARCHAR(20) NOT NULL DEFAULT 'FINAL'
                             CHECK (exam_type IN ('INTERNAL','PRACTICAL','VIVA','EXTERNAL','FINAL')),
    start_date            DATE,
    end_date              DATE,
    status                VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED'
                             CHECK (status IN ('SCHEDULED','ONGOING','MARKS_ENTRY','COMPLETED','PUBLISHED')),
    created_at             TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE exam_schedules (
    id                  BIGSERIAL PRIMARY KEY,
    examination_id        BIGINT NOT NULL REFERENCES examinations(id) ON DELETE CASCADE,
    subject_id            BIGINT NOT NULL REFERENCES subjects(id),
    exam_date              DATE,
    max_marks              NUMERIC(6,2) NOT NULL DEFAULT 100,
    UNIQUE (examination_id, subject_id)
);

CREATE TABLE exam_marks (
    id                  BIGSERIAL PRIMARY KEY,
    examination_id        BIGINT NOT NULL REFERENCES examinations(id) ON DELETE CASCADE,
    subject_id             BIGINT NOT NULL REFERENCES subjects(id),
    student_id             BIGINT NOT NULL REFERENCES students(id),
    component_id           BIGINT NOT NULL REFERENCES mark_components(id),
    marks_obtained         NUMERIC(6,2) NOT NULL,
    max_marks              NUMERIC(6,2) NOT NULL,
    entered_by              BIGINT NOT NULL REFERENCES users(id),
    entered_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by               BIGINT REFERENCES users(id),
    updated_at               TIMESTAMPTZ,
    UNIQUE (examination_id, subject_id, student_id, component_id)
);

CREATE TABLE result_cards (
    id                  BIGSERIAL PRIMARY KEY,
    student_id            BIGINT NOT NULL REFERENCES students(id),
    examination_id        BIGINT NOT NULL REFERENCES examinations(id),
    subject_id            BIGINT NOT NULL REFERENCES subjects(id),
    total_marks_obtained   NUMERIC(6,2) NOT NULL,
    total_max_marks        NUMERIC(6,2) NOT NULL,
    percentage             NUMERIC(5,2) NOT NULL,
    grade_id               BIGINT REFERENCES grade_master(id),
    grade_point            NUMERIC(3,1),
    credits                NUMERIC(3,1) NOT NULL DEFAULT 4,
    status                 VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT','PUBLISHED')),
    version                INT NOT NULL DEFAULT 1,
    computed_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (student_id, examination_id, subject_id)
);

CREATE TABLE result_card_revisions (
    id              BIGSERIAL PRIMARY KEY,
    result_card_id  BIGINT NOT NULL REFERENCES result_cards(id) ON DELETE CASCADE,
    previous_marks  NUMERIC(6,2),
    previous_grade  VARCHAR(5),
    reason          VARCHAR(255),
    revised_by       BIGINT NOT NULL REFERENCES users(id),
    revised_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE semester_results (
    id              BIGSERIAL PRIMARY KEY,
    student_id      BIGINT NOT NULL REFERENCES students(id),
    academic_year_id BIGINT NOT NULL REFERENCES academic_years(id),
    semester        INT NOT NULL,
    sgpa            NUMERIC(4,2),
    cgpa            NUMERIC(4,2),
    total_credits   NUMERIC(5,1),
    status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT','PUBLISHED')),
    computed_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (student_id, academic_year_id, semester)
);

CREATE TABLE result_publications (
    id              BIGSERIAL PRIMARY KEY,
    examination_id  BIGINT NOT NULL REFERENCES examinations(id),
    published_by     BIGINT NOT NULL REFERENCES users(id),
    published_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    remarks           VARCHAR(255)
);

CREATE INDEX idx_exam_marks_student ON exam_marks(student_id);
CREATE INDEX idx_result_cards_student ON result_cards(student_id);
CREATE INDEX idx_semester_results_student ON semester_results(student_id);
