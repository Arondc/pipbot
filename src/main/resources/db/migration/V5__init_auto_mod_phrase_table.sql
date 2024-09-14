create table AUTO_MOD_PHRASE
(
    ID               BIGINT not null primary key,
    TEXT             CHARACTER VARYING(255),
    CHANNEL_ID       BIGINT
);
CREATE SEQUENCE AUTO_MOD_PHRASE_SEQ AS INTEGER;

