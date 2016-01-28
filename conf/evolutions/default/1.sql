# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table token (
  id                        bigint auto_increment not null,
  user_id                   varchar(255),
  token                     varchar(255),
  created_time              bigint,
  is_token_used             varchar(255),
  constraint pk_token primary key (id))
;

create table volume (
  id                        bigint auto_increment not null,
  title                     TEXT,
  volume_id                 varchar(255),
  male_author               TEXT,
  female_author             TEXT,
  gender_unkown_author      TEXT,
  page_count                varchar(255),
  word_count                varchar(255),
  constraint pk_volume primary key (id))
;




# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists token;

drop table if exists volume;

SET REFERENTIAL_INTEGRITY TRUE;

