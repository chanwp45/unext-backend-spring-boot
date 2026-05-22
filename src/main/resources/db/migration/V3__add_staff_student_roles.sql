-- Add STAFF and STUDENT values to user_role enum (PostgreSQL only allows ADD VALUE, not DROP)
ALTER TYPE user_role ADD VALUE IF NOT EXISTS 'STAFF';
ALTER TYPE user_role ADD VALUE IF NOT EXISTS 'STUDENT';
