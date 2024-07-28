create table USERS
(
    ID                BIGINT not null primary key,
    NAME              CHARACTER VARYING(255) not null
);
CREATE SEQUENCE USERS_SEQ AS INTEGER;

create table user_channel_information
(
    user_id BIGINT not null ,
    channel_id BIGINT not null ,
    last_seen  TIMESTAMP,
    amount_of_visited_streams BIGINT,
    constraint USER_CHANNELS_INFORMATION_USER_FK foreign key (user_id) references PUBLIC.USERS,
    constraint USER_CHANNELS_INFORMATION_CHANNEL_FK foreign key (channel_id) references PUBLIC.CHANNELS,
    primary key(user_id, channel_id)
)