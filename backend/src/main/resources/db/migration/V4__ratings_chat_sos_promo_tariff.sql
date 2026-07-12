-- Ratings (passenger -> driver, one per ride/rater).
CREATE TABLE ride_ratings (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ride_id         UUID NOT NULL REFERENCES rides(id) ON DELETE CASCADE,
    rater_id        UUID NOT NULL REFERENCES users(id),
    ratee_id        UUID NOT NULL REFERENCES users(id),
    stars           SMALLINT NOT NULL CHECK (stars BETWEEN 1 AND 5),
    comment         VARCHAR(500),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (ride_id, rater_id)
);

CREATE INDEX idx_ride_ratings_ratee ON ride_ratings(ratee_id);

-- In-ride chat.
CREATE TABLE ride_messages (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ride_id         UUID NOT NULL REFERENCES rides(id) ON DELETE CASCADE,
    sender_id       UUID NOT NULL REFERENCES users(id),
    sender_role     VARCHAR(20) NOT NULL,
    body            VARCHAR(1000) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_ride_messages_ride_id ON ride_messages(ride_id);

-- SOS incidents (safety button).
CREATE TABLE sos_incidents (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ride_id         UUID NOT NULL REFERENCES rides(id) ON DELETE CASCADE,
    user_id         UUID NOT NULL REFERENCES users(id),
    lat             DOUBLE PRECISION NOT NULL,
    lng             DOUBLE PRECISION NOT NULL,
    note            VARCHAR(500),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_sos_incidents_created_at ON sos_incidents(created_at DESC);

-- Promo codes + per-user redemptions.
CREATE TABLE promo_codes (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code            VARCHAR(40) NOT NULL UNIQUE,
    discount_type   VARCHAR(10) NOT NULL CHECK (discount_type IN ('PERCENT', 'FIXED')),
    discount_value  INT NOT NULL CHECK (discount_value > 0),
    max_uses        INT,
    used_count      INT NOT NULL DEFAULT 0,
    active          BOOLEAN NOT NULL DEFAULT true,
    expires_at      TIMESTAMPTZ
);

CREATE TABLE promo_redemptions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    promo_id        UUID NOT NULL REFERENCES promo_codes(id),
    user_id         UUID NOT NULL REFERENCES users(id),
    ride_id         UUID NOT NULL REFERENCES rides(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (promo_id, user_id)
);

INSERT INTO promo_codes (code, discount_type, discount_value, max_uses) VALUES
    ('WELCOME10', 'PERCENT', 10, NULL),
    ('TAXI5', 'FIXED', 5, NULL);

-- Loyalty points ledger (simple running balance on the user).
ALTER TABLE users ADD COLUMN loyalty_points INT NOT NULL DEFAULT 0;

-- Tariffs + fare/promo tracking on rides.
ALTER TABLE rides ADD COLUMN tariff VARCHAR(20) NOT NULL DEFAULT 'ECONOMY'
    CHECK (tariff IN ('ECONOMY', 'COMFORT', 'BUSINESS', 'ELECTRO'));
ALTER TABLE rides ADD COLUMN fare INT;
ALTER TABLE rides ADD COLUMN promo_code VARCHAR(40);
ALTER TABLE rides ADD COLUMN discount_applied INT;
