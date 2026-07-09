-- Extra roles: operator, dispatcher, super admin.
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;
ALTER TABLE users ADD CONSTRAINT users_role_check
    CHECK (role IN ('PASSENGER', 'DRIVER', 'OPERATOR', 'DISPATCHER', 'ADMIN', 'SUPER_ADMIN'));

-- Scheduled ("book for later") rides.
ALTER TABLE rides ADD COLUMN IF NOT EXISTS scheduled_at TIMESTAMPTZ;

ALTER TABLE rides DROP CONSTRAINT IF EXISTS rides_status_check;
ALTER TABLE rides ADD CONSTRAINT rides_status_check CHECK (status IN (
    'SCHEDULED', 'REQUESTED', 'SEARCHING', 'ACCEPTED', 'DRIVER_ARRIVED',
    'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'NO_DRIVERS_FOUND'
));

-- Index for the scheduler sweep (due scheduled rides).
CREATE INDEX IF NOT EXISTS idx_rides_scheduled ON rides(status, scheduled_at);
