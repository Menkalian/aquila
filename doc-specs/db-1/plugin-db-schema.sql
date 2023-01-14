CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

BEGIN TRANSACTION;
CREATE TABLE IF NOT EXISTS file_data
(
    id         uuid
        PRIMARY KEY DEFAULT uuid_generate_v4(),
    name       TEXT      NULL,
    extension  TEXT      NULL,
    created_at TIMESTAMP NOT NULL,
    data       bytea     NOT NULL
);

CREATE TABLE IF NOT EXISTS oauth_link
(
    id     SERIAL
        PRIMARY KEY,
    type   INTEGER
        REFERENCES oauth_type (id),
    userid TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS oauth_type
(
    id   SERIAL
        PRIMARY KEY,
    name TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS plugin_data
(
    id         SERIAL
        PRIMARY KEY,
    name       TEXT
        UNIQUE      NOT NULL,
    owner      INTEGER
        REFERENCES user_data (id),
    searchtext TEXT NOT NULL,
    attributes INTEGER
        REFERENCES variable_list (id)
);

CREATE TABLE IF NOT EXISTS plugin_version
(
    id            SERIAL
        PRIMARY KEY,
    name          TEXT NOT NULL, -- name of the version (e.g. "0.2.5-beta4")
    plugin        INTEGER
        REFERENCES plugin_data (id),
    creation_time TIMESTAMP,
    attributes    INTEGER
        REFERENCES variable_list (id),
    artifact      uuid
        REFERENCES file_data (id)
);

CREATE TABLE IF NOT EXISTS server_data
(
    id                  SERIAL
        PRIMARY KEY,
    configuration_group TEXT
        UNIQUE NOT NULL,
    attributes          INTEGER
        REFERENCES variable_list
);

CREATE TABLE IF NOT EXISTS user_data
(
    id       SERIAL
        PRIMARY KEY,
    auth     INTEGER
        REFERENCES oauth_link (id),
    name     TEXT NOT NULL,
    userdata INTEGER
        REFERENCES variable_list (id)
);

CREATE TABLE IF NOT EXISTS variable
(
    id    SERIAL
        PRIMARY KEY,
    list  INTEGER NOT NULL
        REFERENCES variable_list (id),
    key   TEXT    NOT NULL,
    value TEXT    NOT NULL,
    type  INTEGER
        REFERENCES variable_type (id)
);

CREATE TABLE IF NOT EXISTS variable_list
(
    id SERIAL
        PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS variable_type
(
    id   SERIAL
        PRIMARY KEY,
    name TEXT NOT NULL
);
COMMIT TRANSACTION;
