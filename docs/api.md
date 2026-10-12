# API

All APIs are under `/api/v1`. Main endpoints include kiosk sessions, configurable categories, host search, application submission, pass validation, check-in, check-out, admin dashboard, application listing, and audit log backing data.
# Verification and appearance APIs

* `POST /api/v1/verification/gate-pass` is public and accepts `{ "gatePassId": "ABC-123", "kioskId": "KIOSK-1" }`. The trimmed `gatePassId` is sent to Tasreeh as `passRefNumber`. It returns one normalized Tasreeh result, one normalized Pangu result, and the combined outcome. Approval requires both results to be approved. The Tasreeh result metadata retains the response `data`, `status`, `statusCode`, `message`, and structured `errors`; application-level `FAILED` responses remain rejected even when Tasreeh returns HTTP 200.
* `GET /api/v1/appearance/published` is public and returns only the current published snapshot.
* `GET|POST /api/v1/admin/appearance/**` requires the administrator role. Media uploads use `multipart/form-data` with a `file` part.
* `GET|POST /api/v1/admin/integrations/**` requires the administrator role. Stale `rowVersion` updates return HTTP 409.
* `GET /uploads/appearance/{id}` serves validated appearance media and does not expose filesystem paths.
* `GET /api/v1/screen-flow/published` returns the active kiosk route order and destinations.
* `GET|POST /api/v1/admin/screen-flow/**` loads, validates, saves, and publishes the protected flow draft.
