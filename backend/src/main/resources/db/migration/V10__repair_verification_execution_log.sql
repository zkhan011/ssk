if object_id(N'dbo.verification_execution_log', N'U') is null
begin
  create table dbo.verification_execution_log (
    id uniqueidentifier not null primary key,
    correlation_id varchar(64) not null,
    gate_pass_hash varchar(64) not null,
    integration_key varchar(40) not null,
    outcome varchar(40) not null,
    http_status int null,
    duration_ms bigint not null,
    created_at datetime2 not null default sysutcdatetime()
  );
end;

if not exists (
  select 1
  from sys.indexes
  where name = N'idx_verification_log_created_at'
    and object_id = object_id(N'dbo.verification_execution_log')
)
begin
  create index idx_verification_log_created_at
    on dbo.verification_execution_log(created_at);
end;
