create table appearance_configuration (
  id uuid primary key,
  status varchar(20) not null,
  version_number bigint not null default 0,
  created_at timestamptz not null default now(),
  published_at timestamptz,
  changed_by varchar(100),
  snapshot text not null,
  row_version bigint
);
create index idx_appearance_configuration_status_version on appearance_configuration(status, version_number desc);
