-- ============================================================
-- V4: Attendance management
-- ============================================================

-- One attendance session = a class taught for a subject/section on a given date+period
CREATE TABLE attendance_sessions (
    id                  BIGSERIAL PRIMARY KEY,
    class_section_id     BIGINT NOT NULL REFERENCES class_sections(id),
    subject_id           BIGINT NOT NULL REFERENCES subjects(id),
    faculty_id           BIGINT NOT NULL REFERENCES faculty_profiles(id),
    attendance_date       DATE NOT NULL,
    session_number        INT NOT NULL DEFAULT 1,   -- supports multiple periods/day
    is_locked             BOOLEAN NOT NULL DEFAULT FALSE,
    requires_approval      BOOLEAN NOT NULL DEFAULT FALSE,  -- true if marked after cutoff
    approved_by            BIGINT REFERENCES users(id),
    approved_at            TIMESTAMPTZ,
    created_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (class_section_id, subject_id, attendance_date, session_number)
);

CREATE TABLE attendance_records (
    id              BIGSERIAL PRIMARY KEY,
    session_id      BIGINT NOT NULL REFERENCES attendance_sessions(id) ON DELETE CASCADE,
    student_id      BIGINT NOT NULL REFERENCES students(id),
    status          VARCHAR(15) NOT NULL CHECK (status IN ('PRESENT','ABSENT','LATE','EXCUSED')),
    remarks         VARCHAR(255),
    marked_by        BIGINT NOT NULL REFERENCES users(id),
    marked_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (session_id, student_id)
);

CREATE TABLE attendance_edit_history (
    id              BIGSERIAL PRIMARY KEY,
    record_id       BIGINT NOT NULL REFERENCES attendance_records(id) ON DELETE CASCADE,
    old_status      VARCHAR(15),
    new_status      VARCHAR(15) NOT NULL,
    reason          VARCHAR(255),
    edited_by        BIGINT NOT NULL REFERENCES users(id),
    edited_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_attendance_sessions_date ON attendance_sessions(attendance_date);
CREATE INDEX idx_attendance_records_student ON attendance_records(student_id);
