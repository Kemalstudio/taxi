CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(20)  NOT NULL CHECK (role IN ('PASSENGER', 'DRIVER')),
    full_name       VARCHAR(255) NOT NULL,
    phone           VARCHAR(32),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE driver_profiles (
    user_id         UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    status          VARCHAR(20) NOT NULL DEFAULT 'OFFLINE' CHECK (status IN ('OFFLINE', 'ONLINE', 'BUSY')),
    vehicle_make    VARCHAR(100),
    vehicle_model   VARCHAR(100),
    plate_number    VARCHAR(20),
    rating          NUMERIC(3, 2) NOT NULL DEFAULT 5.00,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE rides (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    passenger_id        UUID NOT NULL REFERENCES users(id),
    driver_id           UUID REFERENCES users(id),
    pickup_lat          DOUBLE PRECISION NOT NULL,
    pickup_lng          DOUBLE PRECISION NOT NULL,
    dropoff_lat         DOUBLE PRECISION NOT NULL,
    dropoff_lng         DOUBLE PRECISION NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'REQUESTED' CHECK (status IN (
                            'REQUESTED', 'SEARCHING', 'ACCEPTED', 'DRIVER_ARRIVED',
                            'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'NO_DRIVERS_FOUND'
                        )),
    requested_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    accepted_at         TIMESTAMPTZ,
    arrived_at          TIMESTAMPTZ,
    started_at          TIMESTAMPTZ,
    completed_at        TIMESTAMPTZ,
    cancelled_at         TIMESTAMPTZ,
    cancelled_reason     VARCHAR(255)
);

CREATE INDEX idx_rides_passenger_id ON rides(passenger_id);
CREATE INDEX idx_rides_driver_id ON rides(driver_id);
CREATE INDEX idx_rides_status ON rides(status);

CREATE TABLE ride_offers (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ride_id         UUID NOT NULL REFERENCES rides(id) ON DELETE CASCADE,
    driver_id       UUID NOT NULL REFERENCES users(id),
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'EXPIRED', 'CANCELLED')),
    offered_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at      TIMESTAMPTZ NOT NULL,
    responded_at    TIMESTAMPTZ
);

CREATE INDEX idx_ride_offers_ride_id ON ride_offers(ride_id);
CREATE INDEX idx_ride_offers_driver_id ON ride_offers(driver_id);
CREATE INDEX idx_ride_offers_status_expires ON ride_offers(status, expires_at);
