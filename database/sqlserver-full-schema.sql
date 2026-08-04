/* Complete SQL Server schema for a fresh Self Kiosk installation. Flyway remains the authoritative migration runner. */
:setvar DatabaseName kiosk
IF DB_ID('$(DatabaseName)') IS NULL EXEC('CREATE DATABASE [' + '$(DatabaseName)' + ']');
GO
USE [$(DatabaseName)];
GO

-- backend/src/main/resources/db/migration/V1__schema.sql
create table visitor_category(id uniqueidentifier primary key,created_at datetime2 not null,updated_at datetime2 not null,version bigint,deleted bit not null,code varchar(50) unique,name_en nvarchar(100),name_ar nvarchar(100),approval_required bit not null);
create table host(id uniqueidentifier primary key,created_at datetime2 not null,updated_at datetime2 not null,version bigint,deleted bit not null,employee_id varchar(50) unique,full_name nvarchar(150),department nvarchar(100),email varchar(150),phone varchar(50));
create table visit_application(id uniqueidentifier primary key,created_at datetime2 not null,updated_at datetime2 not null,version bigint,deleted bit not null,application_number varchar(60) unique,full_name nvarchar(160),category_code varchar(50),document_type varchar(50),masked_document_number varchar(80),company_name nvarchar(160),host_employee_id varchar(50),visit_purpose nvarchar(120),location_name nvarchar(120),gate_name nvarchar(80),valid_from datetime2,valid_until datetime2,status varchar(40),photo_path varchar(500),qr_token varchar(160) unique);
create table audit_log(id uniqueidentifier primary key,created_at datetime2 not null,updated_at datetime2 not null,version bigint,deleted bit not null,actor varchar(100),action varchar(80),entity_type varchar(80),entity_id varchar(80),remarks nvarchar(max));
create table check_in_out_event(id uniqueidentifier primary key,created_at datetime2 not null,updated_at datetime2 not null,version bigint,deleted bit not null,pass_id uniqueidentifier,event_type varchar(40),gate_name nvarchar(80),terminal varchar(80),remarks nvarchar(max));
create index idx_visit_status on visit_application(status);create index idx_qr_token on visit_application(qr_token);
GO

-- backend/src/main/resources/db/migration/V2__seed.sql
insert into visitor_category values(NEWID(),SYSUTCDATETIME(),SYSUTCDATETIME(),0,0,'VISITOR','Visitor',N'زائر',0),(NEWID(),SYSUTCDATETIME(),SYSUTCDATETIME(),0,0,'SEAMAN','Seaman',N'بحار',1),(NEWID(),SYSUTCDATETIME(),SYSUTCDATETIME(),0,0,'CONTRACTOR','Contractor',N'مقاول',1),(NEWID(),SYSUTCDATETIME(),SYSUTCDATETIME(),0,0,'DRIVER','Driver',N'سائق',0),(NEWID(),SYSUTCDATETIME(),SYSUTCDATETIME(),0,0,'DELIVERY','Delivery personnel',N'موظف توصيل',0),(NEWID(),SYSUTCDATETIME(),SYSUTCDATETIME(),0,0,'GOVERNMENT','Government official',N'مسؤول حكومي',1),(NEWID(),SYSUTCDATETIME(),SYSUTCDATETIME(),0,0,'EMPLOYEE_GUEST','Employee guest',N'ضيف موظف',0),(NEWID(),SYSUTCDATETIME(),SYSUTCDATETIME(),0,0,'SERVICE_PROVIDER','Service provider',N'مزود خدمة',1);
insert into host values(NEWID(),SYSUTCDATETIME(),SYSUTCDATETIME(),0,0,'E1001','Aisha Khan','Marine Operations','aisha@example.local','+971500000001'),(NEWID(),SYSUTCDATETIME(),SYSUTCDATETIME(),0,0,'E1002','Omar Ali','Security','omar@example.local','+971500000002');
GO

-- backend/src/main/resources/db/migration/V3__appearance_configuration.sql
create table appearance_configuration (id uniqueidentifier primary key,status varchar(20) not null,version_number bigint not null default 0,created_at datetime2 not null default SYSUTCDATETIME(),published_at datetime2,changed_by varchar(100),snapshot nvarchar(max) not null,row_version bigint);
create index idx_appearance_configuration_status_version on appearance_configuration(status, version_number desc);
GO

-- backend/src/main/resources/db/migration/V4__integration_configuration.sql
create table integration_configuration (
  integration_key varchar(40) primary key,
  enabled bit not null default 1,
  base_url varchar(500),
  connect_timeout_ms int not null default 5000,
  read_timeout_ms int not null default 15000,
  retry_count int not null default 2,
  updated_at datetime2 not null default SYSUTCDATETIME(),
  updated_by varchar(100),
  row_version bigint
);
insert into integration_configuration(integration_key,enabled,connect_timeout_ms,read_timeout_ms,retry_count) values ('TASREEH',1,5000,15000,2),('PANGU',1,5000,15000,2);
GO
-- Apply the versioned Flyway scripts in backend/src/main/resources/db/migration in production.
-- The following tables mirror V5 and V6 for manual SQL Server provisioning.
if col_length('integration_configuration','verification_path') is null alter table integration_configuration add verification_path varchar(300);
if col_length('integration_configuration','approval_field') is null alter table integration_configuration add approval_field varchar(200);
if col_length('integration_configuration','approval_value') is null alter table integration_configuration add approval_value varchar(100);
if object_id('verification_execution_log', 'U') is null
create table verification_execution_log(id uniqueidentifier primary key,correlation_id varchar(64) not null,gate_pass_hash varchar(64) not null,integration_key varchar(40) not null,outcome varchar(40) not null,http_status int,duration_ms bigint not null,created_at datetime2 not null default SYSUTCDATETIME());
if object_id('appearance_media', 'U') is null
create table appearance_media(id uniqueidentifier primary key,stored_name varchar(100) not null unique,content_type varchar(50) not null,size_bytes bigint not null,checksum varchar(64) not null,created_at datetime2 not null default SYSUTCDATETIME(),created_by varchar(100) not null,row_version bigint);
if object_id('screen_flow_configuration', 'U') is null create table screen_flow_configuration(id uniqueidentifier primary key,status varchar(20) not null,version_number bigint not null default 0,snapshot nvarchar(max) not null,created_at datetime2 not null default SYSUTCDATETIME(),published_at datetime2,changed_by varchar(100),row_version bigint);
if col_length('integration_configuration','workflow_snapshot') is null alter table integration_configuration add workflow_snapshot nvarchar(max);
if col_length('integration_configuration','execution_mode') is null alter table integration_configuration add execution_mode varchar(20) not null default 'SEQUENTIAL';
