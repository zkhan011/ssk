# Tasreeh and Pangu API configuration

Administrators open **Tasreeh & Pangu APIs** in the dashboard. Each integration supports enablement, HTTPS base URL, connect/read timeout and retry count. Values persist in SQL Server table `integration_configuration`; write APIs are protected by the ADMIN role. Production credentials and API secrets must be supplied through the integration gateway/environment and are not accepted by this UI.

Endpoints: `GET /api/v1/admin/integrations` and `POST /api/v1/admin/integrations/{TASREEH|PANGU}`. Validation ranges are 500–60,000 ms connect timeout, 500–120,000 ms read timeout and 0–5 retries.

The complete fresh-install SQL Server script is `database/sqlserver-full-schema.sql`. Existing deployments must continue to apply incremental Flyway migration `V4__integration_configuration.sql` instead of rerunning the full script.
