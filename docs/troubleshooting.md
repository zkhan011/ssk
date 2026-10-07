# Troubleshooting

Check SQL Server health, backend `/actuator/health` when actuator is enabled, browser camera permissions, `VITE_API_URL`, CORS configuration, and Docker container logs.

## SQL Server reports `Invalid object name 'verification_execution_log'`

The application requires Flyway migration `V10__repair_verification_execution_log.sql`. It safely creates `dbo.verification_execution_log` and its timestamp index when an older or manually provisioned database is missing them.

1. Confirm the application is connected to the intended `kiosk` database.
2. Restart the backend so Flyway applies pending migrations, or run `mvn flyway:migrate` with the same SQL Server URL and credentials used by the application.
3. Verify migration 10 is successful in `dbo.flyway_schema_history`.
4. Verify `OBJECT_ID(N'dbo.verification_execution_log', N'U')` is not null.

Do not edit or mark the older migration as successful manually. The repair migration is idempotent and is safe for databases where the table already exists.
