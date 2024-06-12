create table AUTO_RESPONSE
(
    ID                BIGINT not null primary key,
    CHANNEL_ID        BIGINT not null,
    COMMAND           CHARACTER VARYING(255),
    MESSAGE           CHARACTER VARYING(1024),
    constraint CHANNELS_AUTO_RESPONSE_CHANNEL_FK foreign key (CHANNEL_ID) references PUBLIC.CHANNELS
);
CREATE SEQUENCE AUTO_RESPONSE_SEQ AS INTEGER;
