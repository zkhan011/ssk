alter table integration_configuration add verification_path varchar(300), approval_field varchar(200), approval_value varchar(100);
update integration_configuration set verification_path='/verify', approval_field='verified', approval_value='true' where integration_key in ('TASREEH','PANGU');

create table verification_execution_log (
  id uniqueidentifier primary key,
  correlation_id varchar(64) not null,
  gate_pass_hash varchar(64) not null,
  integration_key varchar(40) not null,
  outcome varchar(40) not null,
  http_status int,
  duration_ms bigint not null,
  created_at datetime2 not null default SYSUTCDATETIME()
);
create index idx_verification_log_created_at on verification_execution_log(created_at);
