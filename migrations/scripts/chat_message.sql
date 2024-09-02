-- liquibase formatted sql

-- changeset eflerrr:init_chat_message_table
CREATE TABLE IF NOT EXISTS public.chat_message
(
    id              bigserial       NOT NULL PRIMARY KEY,
    message_data    bytea           NOT NULL,
    message_type    text            NOT NULL,
    client_from_id  bigint          NOT NULL,
    client_to_id    bigint          NOT NULL,
    chat_name       text            NOT NULL,
    sent_time       timestamptz     NOT NULL,
    file_name       text,
    file_mime_type  text,

    FOREIGN KEY ("client_from_id")  REFERENCES "client" ("session_id"),
    FOREIGN KEY ("client_to_id")    REFERENCES "client" ("session_id"),
    FOREIGN KEY ("chat_name")       REFERENCES "chat" ("chat_name")
);

-- rollback DROP TABLE "chat_message";
