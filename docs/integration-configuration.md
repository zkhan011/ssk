# Tasreeh and Pangu API configuration

Administrators open **Tasreeh & Pangu APIs** in the dashboard. Each integration supports enablement, HTTPS base URL, connect/read timeout and retry count. Values persist in SQL Server table `integration_configuration`; write APIs are protected by the ADMIN role. Production credentials and API secrets must be supplied through the integration gateway/environment and are not accepted by this UI.

Endpoints: `GET /api/v1/admin/integrations` and `POST /api/v1/admin/integrations/{TASREEH|PANGU}`. Validation ranges are 500–60,000 ms connect timeout, 500–120,000 ms read timeout and 0–5 retries.

The complete fresh-install SQL Server script is `database/sqlserver-full-schema.sql`. Existing deployments must continue to apply the incremental Flyway migrations instead of rerunning the full script. Migration `V9__configure_tasreeh_kiosk_api.sql` provisions the Tasreeh kiosk workflow.
# Tasreeh and Pangu verification

The kiosk calls only `POST /api/v1/verification/gate-pass`. The backend executes the published Tasreeh and Pangu configurations concurrently and approves a pass only when **both** integrations approve it. A failure in one integration returns `PARTIAL_FAILURE`; two unavailable integrations return `VERIFICATION_UNAVAILABLE`.

Administrators configure each HTTPS base URL, verification path, nested approval response field, approval value, connection timeout, read timeout, and retry limit under **Tasreeh & Pangu APIs**. Response fields use restricted dot/index paths such as `data.approved` or `items[0].approved`; executable expressions are not accepted.

`CORS_ALLOWED_ORIGINS` is a comma-separated allowlist. `APPEARANCE_MEDIA_DIRECTORY` controls persistent image storage and `APPEARANCE_MEDIA_MAX_BYTES` defaults to 5 MiB. Uploaded PNG, JPEG, WEBP, and sanitized SVG files receive generated names and checksum-based cache-busting URLs.

## Ordered Tasreeh authentication and pass lookup

The provisioned Tasreeh flow posts an `application/x-www-form-urlencoded` password-grant request to the Dubai Trade token origin and maps `access_token` from its response. It then calls `getKioskPassDetails` on the Tasreeh API origin with `Authorization: Bearer ...`, `Accept-Language: en-US,en;q=0.5`, and `passRefNumber` populated from the scanned gate pass. A workflow step may specify an HTTPS `baseUrl` when it uses a different trusted origin from the integration base URL.

Set `TASREEH_USERNAME`, `TASREEH_PASSWORD`, `TASREEH_CLIENT_ID`, `TASREEH_CLIENT_SECRET`, and `TASREEH_SCOPE` in the backend secret store or environment. Do not enter credentials or bearer tokens in the workflow JSON. Environment-backed `{{secrets.*}}` placeholders are resolved only by the backend and are never returned as values to the browser.

Each step defines `id`, `method`, relative `path`, optional HTTPS origin override `baseUrl`, optional `headers`, `body`, `successStatusCodes`, and response `outputs`. Bodies support JSON (the default) and `application/x-www-form-urlencoded`. Supported methods are GET, POST, PUT, PATCH, and DELETE. Templates accept only `input`, `context`, prior `steps.*.outputs`, and uppercase `secrets` references; SpEL, scripts, file URLs, and arbitrary expressions are rejected. The provisioned contract uses approval field `$httpStatus` with value `200`.
