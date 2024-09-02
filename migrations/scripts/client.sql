-- liquibase formatted sql

-- changeset eflerrr:init_client_table
CREATE TABLE IF NOT EXISTS public.client
(
    session_id          bigserial   NOT NULL PRIMARY KEY,
    client_name         text        NOT NULL,
    encryption_mode     text        NOT NULL,
    padding_type        text        NOT NULL,
    iv                  bytea       NOT NULL
);

-- rollback DROP TABLE "client";
