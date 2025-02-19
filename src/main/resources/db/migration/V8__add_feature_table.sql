create table FEATURES
(
    ID           BIGINT not null primary key,
    NAME         CHARACTER VARYING(255) not null,
    ENABLED      BOOLEAN not null
);

CREATE SEQUENCE FEATURES_SEQ AS INTEGER;