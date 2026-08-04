# Tasreeh and Pangu API configuration

Administrators open **Tasreeh & Pangu APIs** in the dashboard. Each integration supports enablement, HTTPS base URL, connect/read timeout and retry count. Values persist in SQL Server table `integration_configuration`; write APIs are protected by the ADMIN role. Production credentials and API secrets must be supplied through the integration gateway/environment and are not accepted by this UI.

Endpoints: `GET /api/v1/admin/integrations` and `POST /api/v1/admin/integrations/{TASREEH|PANGU}`. Validation ranges are 500–60,000 ms connect timeout, 500–120,000 ms read timeout and 0–5 retries.

The complete fresh-install SQL Server script is `database/sqlserver-full-schema.sql`. Existing deployments must continue to apply incremental Flyway migration `V4__integration_configuration.sql` instead of rerunning the full script.
# Tasreeh and Pangu verification

The kiosk calls only `POST /api/v1/verification/gate-pass`. The backend executes the published Tasreeh and Pangu configurations concurrently and approves a pass only when **both** integrations approve it. A failure in one integration returns `PARTIAL_FAILURE`; two unavailable integrations return `VERIFICATION_UNAVAILABLE`.

Administrators configure each HTTPS base URL, verification path, nested approval response field, approval value, connection timeout, read timeout, and retry limit under **Tasreeh & Pangu APIs**. Response fields use restricted dot/index paths such as `data.approved` or `items[0].approved`; executable expressions are not accepted.

`CORS_ALLOWED_ORIGINS` is a comma-separated allowlist. `APPEARANCE_MEDIA_DIRECTORY` controls persistent image storage and `APPEARANCE_MEDIA_MAX_BYTES` defaults to 5 MiB. Uploaded PNG, JPEG, WEBP, and sanitized SVG files receive generated names and checksum-based cache-busting URLs.

## Ordered authentication and Employee Access Sync

The Tasreeh editor accepts a restricted JSON workflow. A typical flow has an `authenticate` step that maps `accessToken` from its response, followed by an `employeeAccessSync` POST. The second step sets `Authorization` to `Bearer {{steps.authenticate.outputs.accessToken}}` and can include `{{input.gatePassId}}` in the configured pass payload. Environment-backed secret placeholders such as `{{secrets.TASREEH_USERNAME}}` and `{{secrets.TASREEH_PASSWORD}}` are resolved only by the backend and are never returned as values to the browser.

Each step defines `id`, `method`, relative `path`, optional `headers`, JSON `body`, `successStatusCodes`, and response `outputs`. Supported methods are GET, POST, PUT, PATCH, and DELETE. Templates accept only `input`, `context`, prior `steps.*.outputs`, and uppercase `secrets` references; SpEL, scripts, file URLs, and arbitrary expressions are rejected. Use approval field `$httpStatus` with value `200` when the final Tasreeh contract defines success only by HTTP status.
