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
