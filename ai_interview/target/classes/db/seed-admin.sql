-- ======================================================
-- SEED DEFAULT ADMIN
-- ======================================================
-- Default Admin Credentials:
-- Email: admin@example.com
-- Password: Admin@123
-- ======================================================

INSERT INTO Admin (
    s_no,
    admin_id,
    name,
    surname,
    email_address,
    mobile_number,
    password,
    admin_created_date,
    admin_created_time,
    share_id,
    role
) VALUES (
    1,
    UUID(),
    'Vishrut',
    'Rana',
    'vishrut.rana@admin.com',
    '9876543210',
    '$2a$10$9ZpK3R3FzEJmQm9nF2xY7.0ZVxV7vMZ4zY5y9lF1W7Q4b8o1y7l1S',
    CURDATE(),
    CURTIME(),
    UUID(),
    'ADMIN'
);