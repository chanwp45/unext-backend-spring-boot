CREATE TYPE audit_action AS ENUM ('INSERT', 'UPDATE', 'DELETE');

CREATE TABLE audit_logs (
    audit_id            BIGSERIAL       PRIMARY KEY,
    table_name          VARCHAR(100)    NOT NULL,
    record_id           VARCHAR(50)     NOT NULL,
    action              audit_action    NOT NULL,
    old_values          JSONB,
    new_values          JSONB,
    changed_fields      JSONB,
    performed_by        VARCHAR(100)    NOT NULL,
    performed_by_name   VARCHAR(200)    NOT NULL,
    user_role           VARCHAR(50)     NOT NULL,
    ip_address          VARCHAR(45)     NOT NULL,
    user_agent          VARCHAR(500),
    session_id          VARCHAR(100),
    reason              VARCHAR(500),
    document_ref        VARCHAR(100),
    performed_at        TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- Immutable: no UPDATE/DELETE allowed (enforced at application layer)
CREATE INDEX idx_audit_table_record  ON audit_logs(table_name, record_id);
CREATE INDEX idx_audit_performed_by  ON audit_logs(performed_by);
CREATE INDEX idx_audit_performed_at  ON audit_logs(performed_at DESC);
CREATE INDEX idx_audit_action        ON audit_logs(action);
CREATE INDEX idx_audit_table_action  ON audit_logs(table_name, action, performed_at DESC);
