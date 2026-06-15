-- ─────────────────────────────────────────────────────────────
-- Seed roles — INSERT only if they don't already exist
-- ─────────────────────────────────────────────────────────────
INSERT INTO roles (name)
SELECT 'ROLE_USER'
WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE name = 'ROLE_USER'
);

INSERT INTO roles (name)
SELECT 'ROLE_ADMIN'
WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE name = 'ROLE_ADMIN'
);

-- ─────────────────────────────────────────────────────────────
-- Seed default admin user
-- Password: Admin@123 (BCrypt encoded)
-- ─────────────────────────────────────────────────────────────
INSERT INTO users (name, email, password, phone, enabled, created_at)
SELECT
    'Super Admin',
    'admin@showtime.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj6o5DCl.J9.',
    '9999999999',
    true,
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'admin@showtime.com'
);

-- ─────────────────────────────────────────────────────────────
-- Assign ADMIN role to default admin user
-- ─────────────────────────────────────────────────────────────
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'admin@showtime.com'
  AND r.name  = 'ROLE_ADMIN'
  AND NOT EXISTS (
      SELECT 1 FROM user_roles ur
      WHERE ur.user_id = u.id
        AND ur.role_id = r.id
  );