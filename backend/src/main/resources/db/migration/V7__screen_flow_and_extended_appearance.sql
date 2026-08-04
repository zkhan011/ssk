create table screen_flow_configuration (
  id uniqueidentifier primary key,
  status varchar(20) not null,
  version_number bigint not null default 0,
  snapshot nvarchar(max) not null,
  created_at datetime2 not null default SYSUTCDATETIME(),
  published_at datetime2,
  changed_by varchar(100),
  row_version bigint
);
create index idx_screen_flow_status_version on screen_flow_configuration(status,version_number desc);
