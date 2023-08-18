DROP TABLE IF EXISTS endpointHit;

CREATE TABLE IF NOT EXISTS endpointHit (
  id BIGSERIAL NOT NULL,
  app VARCHAR NOT NULL, -- Идентификатор сервиса для которого записывается информация
  uri VARCHAR NOT NULL,  -- URI для которого был осуществлен запрос
  ip VARCHAR NOT NULL,  -- IP-адрес пользователя, осуществившего запрос
  timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW() -- Дата и время, когда был совершен запрос к эндпоинту
  -- (в формате "yyyy-MM-dd HH:mm:ss")
);