# Visitor and Seaman Gate Pass Kiosk

Production-oriented monorepo for a Java 21 Spring Boot backend and React/TypeScript kiosk + administration frontend. The implementation follows the provided Figma prototype where accessible; direct asset extraction was not available in this environment, so the kiosk visual system recreates a maritime kiosk style with large cards, navy/blue/gold tokens, camera overlays, pass cards, RTL support, and touch-first spacing.

## Structure

```text
backend/      Spring Boot 3, JPA, Flyway, SQL Server, OpenAPI
frontend/     React, TypeScript, Vite, TanStack Query, QR generation
docs/         Architecture, ERD, API, deployment and operations guides
deployment/   Nginx and environment templates
database/     Database notes and migration entrypoints
```

## Development credentials

Development seed data includes host employees `E1001` and `E1002`. Production must configure real users and secrets through environment variables.

## Exact commands

```bash
# 1. Start SQL Server
docker compose up -d sqlserver

# 2. Run database migrations
cd backend && mvn flyway:migrate -Dflyway.url=jdbc:sqlserver://localhost:1433;databaseName=kiosk;encrypt=true;trustServerCertificate=true -Dflyway.user=sa -Dflyway.password=ChangeThis_DevelopmentPassword1

# 3. Start Spring Boot backend
cd backend && mvn spring-boot:run

# 4. Start React frontend
cd frontend && npm install && npm run dev

# 5. Start complete stack
docker compose up --build

# 6. Run backend tests
cd backend && mvn test

# 7. Run frontend tests
cd frontend && npm install && npm test

# 8. Build production artifacts
cd backend && mvn clean package && cd ../frontend && npm install && npm run build
```

## Implemented workflows

- Kiosk welcome, language, category, document type, document capture, OCR review, live face capture, visit details, review/consent, submission, printable QR pass, validation.
- Administration routes for dashboard, applications, approvals, visitors inside, passes, configuration, kiosks, users, and audit logs.
- Backend APIs under `/api/v1`, Flyway schema/seed data, configurable visitor categories, host lookup, pass validation, check-in/check-out, audit events.
- English/Arabic runtime switching, RTL layout, idle timeout cleanup, offline status indicator, session storage cleanup.

## API documentation

Run the backend and open `http://localhost:8080/swagger-ui.html`.
## Runtime configuration added for kiosk administration

Set `CORS_ALLOWED_ORIGINS` to the comma-separated browser origins permitted to call the backend (defaults to the two Vite origins on `localhost` and `127.0.0.1`). Set `APPEARANCE_MEDIA_DIRECTORY` to a persistent, writable directory and optionally change `APPEARANCE_MEDIA_MAX_BYTES` from its 5 MiB default. Tasreeh and Pangu base URLs and response mappings are managed from `/admin/integrations`; the browser never receives integration credentials or raw provider responses.

The live kiosk submits variable-length alphanumeric pass IDs to `POST /api/v1/verification/gate-pass`. The backend always invokes both configured providers and only returns `APPROVED` when both approve.
