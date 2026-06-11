-- ── Levels ───────────────────────────────────────────────────────────────────

CREATE TABLE levels (
    id               VARCHAR(10)  PRIMARY KEY,
    name             VARCHAR(10)  NOT NULL,
    description      TEXT         NOT NULL,
    short_description VARCHAR(255) NOT NULL
);

CREATE TABLE level_catalog (
    id       UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    level_id VARCHAR(10) NOT NULL REFERENCES levels(id),
    type     VARCHAR(50) NOT NULL,
    is_free  BOOLEAN     NOT NULL
);

-- ── Providers ─────────────────────────────────────────────────────────────────

CREATE TABLE providers (
    id          VARCHAR(50)  PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    full_name   VARCHAR(255) NOT NULL,
    description TEXT         NOT NULL,
    logo        VARCHAR(255) NOT NULL,
    website     VARCHAR(255) NOT NULL
);

CREATE TABLE provider_levels (
    provider_id VARCHAR(50) NOT NULL REFERENCES providers(id),
    level_id    VARCHAR(10) NOT NULL REFERENCES levels(id),
    PRIMARY KEY (provider_id, level_id)
);

-- ── Products ──────────────────────────────────────────────────────────────────

CREATE TABLE products (
    id                     VARCHAR(20) PRIMARY KEY,
    level_id               VARCHAR(10) NOT NULL REFERENCES levels(id),
    price_cents            INTEGER     NOT NULL,
    discounted_price_cents INTEGER,
    currency               VARCHAR(10) NOT NULL
);

-- ── Users ─────────────────────────────────────────────────────────────────────

CREATE TABLE users (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    google_id       VARCHAR(255) NOT NULL,
    name            VARCHAR(100) NOT NULL,
    email           VARCHAR(255) NOT NULL,
    profile_picture VARCHAR(500),
    created_at      TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT uq_users_google_id UNIQUE (google_id),
    CONSTRAINT uq_users_email     UNIQUE (email)
);

CREATE TABLE user_products (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID        NOT NULL REFERENCES users(id),
    product_id   VARCHAR(20) NOT NULL,
    purchased_at TIMESTAMP   NOT NULL DEFAULT now(),
    CONSTRAINT uq_user_products UNIQUE (user_id, product_id)
);

CREATE INDEX idx_user_products_user_id ON user_products(user_id);

CREATE TABLE refresh_tokens (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID         NOT NULL REFERENCES users(id),
    token      VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP    NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT uq_refresh_tokens_token UNIQUE (token)
);

-- ── Exam details ──────────────────────────────────────────────────────────────

CREATE TABLE exam_details (
    id            VARCHAR(50)  PRIMARY KEY,
    provider_id   VARCHAR(50)  NOT NULL REFERENCES providers(id),
    level_id      VARCHAR(10)  NOT NULL REFERENCES levels(id),
    name          VARCHAR(255) NOT NULL,
    total_points  DOUBLE PRECISION NOT NULL,
    total_minutes INTEGER      NOT NULL,
    data          TEXT         NOT NULL
);

-- ── Exams ─────────────────────────────────────────────────────────────────────

CREATE TABLE exams (
    id             VARCHAR(50)  PRIMARY KEY,
    exam_detail_id VARCHAR(50)  NOT NULL REFERENCES exam_details(id),
    level_id       VARCHAR(10)  NOT NULL REFERENCES levels(id),
    provider_id    VARCHAR(50)  NOT NULL REFERENCES providers(id),
    name           VARCHAR(255) NOT NULL,
    is_free        BOOLEAN      NOT NULL DEFAULT false,
    total_points   DOUBLE PRECISION NOT NULL,
    total_minutes  INTEGER      NOT NULL,
    data           TEXT         NOT NULL
);

-- ── Bugs ──────────────────────────────────────────────────────────────────────

CREATE TABLE bugs (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    title       VARCHAR(255) NOT NULL,
    description TEXT         NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT now()
);
