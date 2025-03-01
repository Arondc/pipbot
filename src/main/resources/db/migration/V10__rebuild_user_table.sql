-- Take username from user table
alter table USER_CHANNEL_INFORMATION add column USER_NAME CHARACTER VARYING(100);
UPDATE USER_CHANNEL_INFORMATION uci set USER_NAME = (SELECT name FROM USERS u where u.id=uci.user_id) where uci.USER_NAME is null;
alter table USER_CHANNEL_INFORMATION alter column USER_NAME CHARACTER VARYING(100) not null;

-- Remove foreign key relation to user table
alter table USER_CHANNEL_INFORMATION drop constraint USER_CHANNELS_INFORMATION_USER_FK;
-- Remove primary key (contains USER_ID)
alter table USER_CHANNEL_INFORMATION drop primary key;
-- Create new primary key
ALTER TABLE USER_CHANNEL_INFORMATION ADD PRIMARY KEY (USER_NAME,CHANNEL_ID);

alter table USER_CHANNEL_INFORMATION drop column USER_ID;

drop table USERS;
