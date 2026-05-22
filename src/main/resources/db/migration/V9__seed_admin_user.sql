INSERT INTO users (id, email, password_hash, role, is_active, created_at, updated_at)
VALUES (uuid_generate_v4(), 'admin@test.com', '$2a$12$t9SJxHBZnG6Ro1V9KMTO2O8Dx/5TmekeXeM0wEPtgxBztrHwa6RJW', 'ADMIN', TRUE, NOW(), NOW())
ON CONFLICT (email) DO NOTHING;
