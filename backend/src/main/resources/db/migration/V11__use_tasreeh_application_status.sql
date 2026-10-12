update integration_configuration
set approval_field = 'status',
    approval_value = 'SUCCESS',
    updated_at = sysutcdatetime(),
    updated_by = 'V11_TASREEH_APPLICATION_STATUS'
where integration_key = 'TASREEH';
