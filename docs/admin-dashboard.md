# Admin dashboard

Open `/login` and authenticate with the administrator credentials supplied through `ADMIN_USERNAME` and `ADMIN_PASSWORD`. All `/api/v1/admin/**` APIs, application details, and security check-in/check-out APIs require the `ADMIN` role and return HTTP 403 for authenticated users without it.

The dashboard provides summary metrics, Appearance Settings, the controlled current Screen Flow, paginated Visitor Registrations, Reports, Roles and Permissions navigation, and Audit History navigation. Registration search is server-side, page sizes are capped at 100, sorting uses an allowlist, identity-document values remain masked, and CSV export is capped at 1,000 masked rows.

Appearance settings retain the existing draft/publish model. The kiosk only reads `/api/v1/appearance/published`, uses bundled defaults while loading, and falls back safely when the endpoint is unavailable. Variable-length alphanumeric Gate Pass IDs remain limited to 64 characters.
