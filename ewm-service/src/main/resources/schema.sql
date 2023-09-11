DROP TABLE IF EXISTS compilations_events, compilations, requests, events, locations, categories, users;

CREATE TABLE IF NOT EXISTS users (
  id BIGSERIAL NOT NULL,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(512) NOT NULL,
  CONSTRAINT PK_USER PRIMARY KEY (id),
  CONSTRAINT UQ_USER_EMAIL UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS categories (
  id BIGSERIAL NOT NULL,
  name VARCHAR(255) NOT NULL,
  CONSTRAINT PK_CATEGORY PRIMARY KEY (id),
  CONSTRAINT UQ_CATEGORY_NAME UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS locations (
  id BIGSERIAL NOT NULL,
  lat FLOAT NOT NULL,
  lon FLOAT NOT NULL,
  CONSTRAINT PK_LOCATION PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS events (
  id BIGSERIAL NOT NULL,
  annotation VARCHAR(2000) NOT NULL,
  category_id BIGINT NOT NULL,
  confirmed_requests BIGINT,
  created_on TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  description VARCHAR(7000) NOT NULL,
  event_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  initiator_id BIGINT NOT NULL,
  location_id BIGINT NOT NULL,
  paid BOOLEAN DEFAULT FALSE,
  participant_limit BIGINT,
  published_on TIMESTAMP WITHOUT TIME ZONE,
  request_moderation BOOLEAN DEFAULT TRUE,
  state VARCHAR(50),
  title VARCHAR(120),
  CONSTRAINT PK_EVENT PRIMARY KEY (id),
  CONSTRAINT FK_EVENTS_CATEGORIES_ID FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE CASCADE,
  CONSTRAINT FK_EVENTS_LOCATIONS_ID_FK FOREIGN KEY (location_id) REFERENCES locations (id) ON DELETE CASCADE,
  CONSTRAINT FK_EVENTS_USERS_ID_FK FOREIGN KEY (initiator_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS requests (
  id BIGSERIAL NOT NULL,
  created TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  event_id BIGINT NOT NULL,
  requester_id BIGINT NOT NULL,
  status VARCHAR(50) NOT NULL,
  CONSTRAINT PK_REQUEST PRIMARY KEY (id),
  CONSTRAINT FK_REQUESTS_EVENTS_ID FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE,
  CONSTRAINT FK_REQUESTS_USERS_ID_FK FOREIGN KEY (requester_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS compilations (
  id BIGSERIAL NOT NULL,
  pinned BOOLEAN,
  title VARCHAR(255) NOT NULL,
  CONSTRAINT PK_COMPILATION PRIMARY KEY (id),
  CONSTRAINT UQ_COMPILATIONS_TITLE UNIQUE (title)
);

CREATE TABLE IF NOT EXISTS compilations_events (
  compilation_id BIGINT REFERENCES compilations (id) ON DELETE CASCADE,
  event_id BIGINT REFERENCES events (id) ON DELETE CASCADE,
  CONSTRAINT pk_compilations_events PRIMARY KEY (compilation_id, event_id),
  CONSTRAINT fk_compilations FOREIGN KEY (compilation_id) REFERENCES compilations (id) ON DELETE CASCADE,
  CONSTRAINT fk_events FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE
);