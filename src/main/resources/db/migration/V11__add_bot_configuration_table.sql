create table CONFIGURATION
(
    ID           BIGINT not null primary key,
    USERNAME         CHARACTER VARYING(255) not null,
    O_AUTH_TOKEN       CHARACTER VARYING(255) not null,
    CLIENT_SECRET     CHARACTER VARYING(255) not null,
    CLIENT_ID     CHARACTER VARYING(255) not null
);
