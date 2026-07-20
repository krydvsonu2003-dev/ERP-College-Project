-- ============================================================
-- V1: Core security schema - users, roles, privileges, audit
-- ============================================================

CREATE TABLE roles (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(50)  NOT NULL UNIQUE,
    description     VARCHAR(255),
    is_system_role  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE privileges (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(100) NOT NULL UNIQUE,   -- e.g. STUDENT_CREATE
    module          VARCHAR(50)  NOT NULL,          -- e.g. STUDENT
    action          VARCHAR(50)  NOT NULL,          -- e.g. CREATE / READ / UPDATE / DELETE / APPROVE / PUBLISH
    description     VARCHAR(255),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE role_privileges (
    role_id         BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    privilege_id    BIGINT NOT NULL REFERENCES privileges(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, privilege_id)
);

CREATE TABLE users (
    id                      BIGSERIAL PRIMARY KEY,
    username                VARCHAR(100) NOT NULL UNIQUE,
    email                   VARCHAR(150) UNIQUE,
    password_hash           VARCHAR(255) NOT NULL,
    full_name               VARCHAR(150) NOT NULL,
    phone                   VARCHAR(20),
    status                  VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                                CHECK (status IN ('ACTIVE','INACTIVE','LOCKED','PENDING')),
    failed_login_attempts  INT          NOT NULL DEFAULT 0,
    locked_until            TIMESTAMPTZ,
    last_login_at           TIMESTAMPTZ,
    must_change_password    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by              BIGINT,
    updated_by               BIGINT,
    created_at               TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at               TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted                  BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE user_roles (
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id     BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE password_reset_tokens (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token       VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ  NOT NULL,
    used        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE audit_logs (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT,
    username        VARCHAR(100),
    role_names      VARCHAR(255),
    action          VARCHAR(100)  NOT NULL,   -- e.g. USER_CREATE, ADMISSION_APPROVE
    entity_name     VARCHAR(100)  NOT NULL,
    entity_id       VARCHAR(50),
    before_value    TEXT,
    after_value     TEXT,
    ip_address      VARCHAR(64),
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_name, entity_id);
CREATE INDEX idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
CREATE INDEX idx_users_status ON users(status);
