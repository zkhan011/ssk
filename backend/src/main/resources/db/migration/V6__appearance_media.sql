create table appearance_media (
  id uniqueidentifier primary key,
  stored_name varchar(100) not null unique,
  content_type varchar(50) not null,
  size_bytes bigint not null,
  checksum varchar(64) not null,
  created_at datetime2 not null default SYSUTCDATETIME(),
  created_by varchar(100) not null,
  row_version bigint
);
