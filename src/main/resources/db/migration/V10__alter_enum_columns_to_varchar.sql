-- Convert native PostgreSQL enum columns to VARCHAR for Hibernate STRING mapping compatibility
ALTER TABLE users        ALTER COLUMN role           TYPE VARCHAR(20) USING role::VARCHAR;
ALTER TABLE curricula    ALTER COLUMN status         TYPE VARCHAR(20) USING status::VARCHAR;
ALTER TABLE students     ALTER COLUMN gender         TYPE VARCHAR(10) USING gender::VARCHAR;
ALTER TABLE students     ALTER COLUMN student_status TYPE VARCHAR(20) USING student_status::VARCHAR;
ALTER TABLE audit_logs   ALTER COLUMN action         TYPE VARCHAR(50) USING action::VARCHAR;
