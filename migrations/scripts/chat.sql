-- liquibase formatted sql

-- changeset eflerrr:init_chat_table
CREATE TABLE IF NOT EXISTS public.chat
(
    chat_name               text    NOT NULL PRIMARY KEY,
    encryption_algorithm    text    NOT NULL
);

-- rollback DROP TABLE "chat";
