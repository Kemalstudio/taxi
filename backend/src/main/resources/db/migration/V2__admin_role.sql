-- Allow the ADMIN role on users (V1 only permitted PASSENGER/DRIVER).
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;
ALTER TABLE users ADD CONSTRAINT users_role_check CHECK (role IN ('PASSENGER', 'DRIVER', 'ADMIN'));
