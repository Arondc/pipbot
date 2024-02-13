create table CHANNELS
(
    ID           BIGINT not null primary key,
    NAME         CHARACTER VARYING(255)
);
CREATE SEQUENCE CHANNELS_SEQ AS INTEGER;

create table MEMES
(
    ID           BIGINT not null primary key,
    LINK         CHARACTER VARYING(255),
    MESSAGE      CHARACTER VARYING(255),
    RECORDED_AT  TIMESTAMP,
    SENT_BY_USER CHARACTER VARYING(255),
    CHANNEL_ID   BIGINT,
    STREAM_ID    BIGINT,
    constraint   MEMES_CHANNEL_FK foreign key (CHANNEL_ID) references PUBLIC.CHANNELS
);

CREATE SEQUENCE MEMES_SEQ AS INTEGER;

create table STREAMS
(
    ID           BIGINT not null primary key,
    START        TIMESTAMP,
    CHANNEL_ID   BIGINT,
    MERGED_TO_ID BIGINT,
    constraint   STREAMS_CHANNEL_FK foreign key (CHANNEL_ID) references PUBLIC.CHANNELS,
    constraint   STREAMS_MERGED_TO_STREAM_FK foreign key (MERGED_TO_ID) references PUBLIC.STREAMS
);

CREATE SEQUENCE STREAMS_SEQ AS INTEGER;

CREATE TABLE QUOTES
(
    ID           BIGINT not null primary key,
    TEXT         CHARACTER VARYING(255),
    NUMBER       BIGINT,
    CHANNEL_ID   BIGINT,
    constraint   QUOTES_CHANNEL_FK foreign key (CHANNEL_ID) references PUBLIC.CHANNELS
);

CREATE SEQUENCE QUOTES_SEQ AS INTEGER;