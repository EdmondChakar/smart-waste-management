----------------------------------------------------------------------------------------------------------

CREATE TABLE roles (
  role_id    SERIAL PRIMARY KEY,
  role_code  VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE user_statuses (
  status_id    SERIAL PRIMARY KEY,
  status_code  VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE users (
  user_id        SERIAL PRIMARY KEY,
  email          VARCHAR(255) NOT NULL UNIQUE,
  password_hash  TEXT NOT NULL,
  role_id        INTEGER NOT NULL REFERENCES roles(role_id),
  status_id      INTEGER NOT NULL REFERENCES user_statuses(status_id),
  created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

----------------------------------------------------------------------------------------------------------

CREATE TABLE bins (
  bin_id       SERIAL PRIMARY KEY,
  public_code  VARCHAR(100) NOT NULL UNIQUE,
  is_active    BOOLEAN NOT NULL,
  created_at   TIMESTAMP NOT NULL
);

----------------------------------------------------------------------------------------------------------

CREATE TABLE devices (
  device_id     SERIAL PRIMARY KEY,
  bin_id        INTEGER NOT NULL REFERENCES bins(bin_id),
  device_uid    VARCHAR(100) NOT NULL UNIQUE,
  api_key_hash  TEXT NOT NULL,
  last_seen_at  TIMESTAMP,
  is_active     BOOLEAN NOT NULL
);

----------------------------------------------------------------------------------------------------------

CREATE TABLE sensor_reading (
  reading_id   SERIAL PRIMARY KEY,
  bin_id       INTEGER NOT NULL REFERENCES bins(bin_id),
  device_id    INTEGER NOT NULL REFERENCES devices(device_id),
  captured_at  TIMESTAMP NOT NULL,
  fill_pct     NUMERIC,
  weight_kg    NUMERIC,
  gps_lat      NUMERIC NULL,
  gps_lon      NUMERIC NULL
);

CREATE INDEX idx_sensor_reading_captured_at
  ON sensor_reading(captured_at);

----------------------------------------------------------------------------------------------------------

CREATE TABLE bin_status (
  bin_id           INTEGER PRIMARY KEY REFERENCES bins(bin_id),
  updated_at       TIMESTAMP NOT NULL,
  fill_pct         NUMERIC,
  weight_kg        NUMERIC,
  lat              NUMERIC,
  lon              NUMERIC,
  is_full          BOOLEAN NOT NULL,
  last_reading_id  INTEGER REFERENCES sensor_reading(reading_id)
);

----------------------------------------------------------------------------------------------------------

CREATE TABLE disposal_session (
  session_id   SERIAL PRIMARY KEY,
  bin_id       INTEGER NOT NULL REFERENCES bins(bin_id),
  device_id    INTEGER NOT NULL REFERENCES devices(device_id),
  opened_at    TIMESTAMP NOT NULL,
  closed_at    TIMESTAMP,
  item_count   INTEGER,
  state        VARCHAR(20) NOT NULL,
  expires_at   TIMESTAMP,
  claimed_at   TIMESTAMP NULL,

  CONSTRAINT chk_disposal_state CHECK (state IN ('OPEN','PENDING_CLAIM','CLAIMED','EXPIRED'))
);

CREATE INDEX idx_disposal_bin_state_expires
  ON disposal_session(bin_id, state, expires_at);

CREATE INDEX idx_disposal_bin_closed_desc
  ON disposal_session(bin_id, closed_at DESC);

----------------------------------------------------------------------------------------------------------

CREATE TABLE scan_event (
  scan_id               SERIAL PRIMARY KEY,
  user_id               INTEGER NOT NULL REFERENCES users(user_id),
  bin_id                INTEGER NOT NULL REFERENCES bins(bin_id),
  scan_at               TIMESTAMP NOT NULL,
  is_valid              BOOLEAN NOT NULL,
  invalid_reason        TEXT NULL,
  total_points_awarded  INTEGER NULL
);

----------------------------------------------------------------------------------------------------------

CREATE TABLE scan_session_claim (
  scan_id             INTEGER NOT NULL REFERENCES scan_event(scan_id),
  session_id          INTEGER NOT NULL REFERENCES disposal_session(session_id),
  item_count_snapshot INTEGER NOT NULL,

  PRIMARY KEY (scan_id, session_id),
  UNIQUE (session_id)
);

----------------------------------------------------------------------------------------------------------

CREATE TABLE points_txn (
  txn_id      SERIAL PRIMARY KEY,
  user_id     INTEGER NOT NULL REFERENCES users(user_id),
  scan_id     INTEGER UNIQUE NULL REFERENCES scan_event(scan_id),
  type        VARCHAR(10) NOT NULL,
  points      INTEGER NOT NULL,
  created_at  TIMESTAMP NOT NULL,

  CONSTRAINT chk_points_type CHECK (type IN ('EARN','REDEEM','ADJUST'))
);

----------------------------------------------------------------------------------------------------------

CREATE TABLE alerts (
  alert_id           SERIAL PRIMARY KEY,
  bin_id             INTEGER NOT NULL REFERENCES bins(bin_id),
  type               VARCHAR(10) NOT NULL,
  status             VARCHAR(10) NOT NULL,
  triggered_at       TIMESTAMP NOT NULL,
  closed_at          TIMESTAMP NULL,
  closed_by_user_id  INTEGER NULL REFERENCES users(user_id),

  CONSTRAINT chk_alert_type CHECK (type IN ('FILL','WEIGHT','OFFLINE')),

  CONSTRAINT chk_alert_status CHECK (status IN ('OPEN','CLOSED'))
);

----------------------------------------------------------------------------------------------------------

CREATE TABLE rewards (
  reward_id    SERIAL PRIMARY KEY,
  title        VARCHAR(255),
  description  TEXT,
  points_cost  INTEGER,
  is_active    BOOLEAN
);

----------------------------------------------------------------------------------------------------------

CREATE TABLE redemption_statuses (
  status_id    SERIAL PRIMARY KEY,
  status_code  VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE redemptions (
  redemption_id  SERIAL PRIMARY KEY,
  user_id        INTEGER NOT NULL REFERENCES users(user_id),
  reward_id      INTEGER NOT NULL REFERENCES rewards(reward_id),
  points_spent   INTEGER,
  status_id      INTEGER NOT NULL REFERENCES redemption_statuses(status_id),
  voucher_code   VARCHAR(255) NULL,
  requested_at   TIMESTAMP NOT NULL,
  fulfilled_at   TIMESTAMP NULL
);

----------------------------------------------------------------------------------------------------------