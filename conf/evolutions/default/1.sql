# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table active_job (
  id                        bigint auto_increment not null,
  job_id                    varchar(255),
  job_name                  varchar(255),
  last_modified             varchar(255),
  status                    varchar(255),
  constraint pk_active_job primary key (id))
;

create table algorithm (
  id                        bigint auto_increment not null,
  name                      varchar(255),
  description               varchar(255),
  authors                   varchar(255),
  version                   varchar(255),
  constraint pk_algorithm primary key (id))
;

create table completed_job (
  id                        bigint auto_increment not null,
  job_id                    varchar(255),
  job_name                  varchar(255),
  last_modified             varchar(255),
  status                    varchar(255),
  job_saved_str             varchar(255),
  constraint pk_completed_job primary key (id))
;

create table token (
  id                        bigint auto_increment not null,
  user_id                   varchar(255),
  token                     varchar(255),
  created_time              bigint,
  is_token_used             varchar(255),
  constraint pk_token primary key (id))
;

create table user (
  user_id                   varchar(255) not null,
  email                     varchar(255),
  user_first_name           varchar(255),
  user_last_name            varchar(255),
  no_of_my_worksets         integer,
  no_of_all_worksets        integer,
  no_of_active_jobs         integer,
  no_of_completed_jobs      integer,
  constraint pk_user primary key (user_id))
;

create table virtual_machine (
  id                        bigint auto_increment not null,
  vm_id                     varchar(255),
  vm_status                 varchar(255),
  mode                      varchar(255),
  constraint pk_virtual_machine primary key (id))
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

SET FOREIGN_KEY_CHECKS=0;

drop table active_job;

drop table algorithm;

drop table completed_job;

drop table token;

drop table user;

drop table virtual_machine;

drop table volume;

SET FOREIGN_KEY_CHECKS=1;

