update integration_configuration
set base_url = 'https://apit.dubaitrade.ae',
    verification_path = '/ext/v1/api/tasreeh/getKioskPassDetails',
    approval_field = '$httpStatus',
    approval_value = '200',
    execution_mode = 'SEQUENTIAL',
    workflow_snapshot = N'{"steps":[{"id":"authenticate","method":"POST","baseUrl":"https://autht.dubaitrade.ae","path":"/auth/realms/DTAPI/protocol/openid-connect/token","headers":{"Accept-Language":"en-US,en;q=0.5"},"contentType":"application/x-www-form-urlencoded","body":{"username":"{{secrets.TASREEH_USERNAME}}","password":"{{secrets.TASREEH_PASSWORD}}","client_id":"{{secrets.TASREEH_CLIENT_ID}}","client_secret":"{{secrets.TASREEH_CLIENT_SECRET}}","scope":"{{secrets.TASREEH_SCOPE}}","grant_type":"password"},"successStatusCodes":[200],"outputs":{"accessToken":"access_token"}},{"id":"getKioskPassDetails","method":"POST","path":"/ext/v1/api/tasreeh/getKioskPassDetails","headers":{"Accept-Language":"en-US,en;q=0.5","Authorization":"Bearer {{steps.authenticate.outputs.accessToken}}"},"body":{"passRefNumber":"{{input.gatePassId}}"},"successStatusCodes":[200],"outputs":{}}]}',
    updated_at = SYSUTCDATETIME(),
    updated_by = 'V9_TASREEH_KIOSK_API'
where integration_key = 'TASREEH';
