alter table integration_configuration add workflow_snapshot nvarchar(max), execution_mode varchar(20) not null default 'SEQUENTIAL';
