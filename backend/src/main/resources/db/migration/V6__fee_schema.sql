-- ============================================================
-- V6: Fee management & payment tracking
-- ============================================================

CREATE TABLE fee_heads (
    id              BIGSERIAL PRIMARY KEY,
    name             VARCHAR(100) NOT NULL UNIQUE,  -- Admission Fee, Tuition Fee, Exam Fee, Hostel Fee...
    code             VARCHAR(20)  NOT NULL UNIQUE,
    description      VARCHAR(255),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE fee_structures (
    id              BIGSERIAL PRIMARY KEY,
    course_id        BIGINT NOT NULL REFERENCES courses(id),
    academic_year_id BIGINT NOT NULL REFERENCES academic_years(id),
    semester          INT NOT NULL,
    fee_head_id        BIGINT NOT NULL REFERENCES fee_heads(id),
    category           VARCHAR(30) NOT NULL DEFAULT 'GENERAL',
    amount             NUMERIC(12,2) NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (course_id, academic_year_id, semester, fee_head_id, category)
);

CREATE TABLE student_fee_assignments (
    id                  BIGSERIAL PRIMARY KEY,
    student_id            BIGINT NOT NULL REFERENCES students(id),
    fee_structure_id      BIGINT NOT NULL REFERENCES fee_structures(id),
    applicable_amount      NUMERIC(12,2) NOT NULL,
    waived_amount           NUMERIC(12,2) NOT NULL DEFAULT 0,
    assigned_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (student_id, fee_structure_id)
);

CREATE TABLE fee_invoices (
    id              BIGSERIAL PRIMARY KEY,
    student_id        BIGINT NOT NULL REFERENCES students(id),
    academic_year_id   BIGINT NOT NULL REFERENCES academic_years(id),
    semester            INT NOT NULL,
    total_amount         NUMERIC(12,2) NOT NULL,
    paid_amount           NUMERIC(12,2) NOT NULL DEFAULT 0,
    due_amount            NUMERIC(12,2) NOT NULL,
    status                VARCHAR(20) NOT NULL DEFAULT 'DUE'
                            CHECK (status IN ('DUE','PARTIALLY_PAID','PAID','OVERDUE')),
    due_date              DATE,
    created_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (student_id, academic_year_id, semester)
);

CREATE TABLE payments (
    id                  BIGSERIAL PRIMARY KEY,
    invoice_id            BIGINT NOT NULL REFERENCES fee_invoices(id),
    student_id            BIGINT NOT NULL REFERENCES students(id),
    amount                 NUMERIC(12,2) NOT NULL CHECK (amount > 0),
    payment_mode           VARCHAR(20) NOT NULL CHECK (payment_mode IN ('CASH','CARD','UPI','NEFT','CHEQUE','ONLINE')),
    payment_reference       VARCHAR(100),
    status                  VARCHAR(20) NOT NULL DEFAULT 'POSTED' CHECK (status IN ('POSTED','REVERSED')),
    received_by              BIGINT NOT NULL REFERENCES users(id),
    paid_at                  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at                TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE payment_allocations (
    id              BIGSERIAL PRIMARY KEY,
    payment_id        BIGINT NOT NULL REFERENCES payments(id) ON DELETE CASCADE,
    fee_head_id        BIGINT NOT NULL REFERENCES fee_heads(id),
    allocated_amount    NUMERIC(12,2) NOT NULL
);

CREATE TABLE receipts (
    id              BIGSERIAL PRIMARY KEY,
    payment_id        BIGINT NOT NULL UNIQUE REFERENCES payments(id),
    receipt_number     VARCHAR(40) NOT NULL UNIQUE,
    generated_by        BIGINT NOT NULL REFERENCES users(id),
    generated_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE fee_waivers (
    id                          BIGSERIAL PRIMARY KEY,
    student_fee_assignment_id     BIGINT NOT NULL REFERENCES student_fee_assignments(id),
    waiver_amount                  NUMERIC(12,2) NOT NULL,
    reason                          VARCHAR(255) NOT NULL,
    approved_by                     BIGINT NOT NULL REFERENCES users(id),
    approved_at                      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE refund_transactions (
    id              BIGSERIAL PRIMARY KEY,
    payment_id        BIGINT NOT NULL REFERENCES payments(id),
    refund_amount       NUMERIC(12,2) NOT NULL,
    reason               VARCHAR(255) NOT NULL,
    processed_by          BIGINT NOT NULL REFERENCES users(id),
    processed_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_fee_invoices_student ON fee_invoices(student_id);
CREATE INDEX idx_fee_invoices_status ON fee_invoices(status);
CREATE INDEX idx_payments_student ON payments(student_id);
