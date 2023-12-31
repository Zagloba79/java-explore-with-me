DROP TABLE IF EXISTS endpoint_hit;

CREATE TABLE IF NOT EXISTS endpoint_hit (
  id BIGSERIAL NOT NULL,
  app VARCHAR(32) NOT NULL,
  uri VARCHAR NOT NULL,
  ip VARCHAR(15) NOT NULL,
  timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW());