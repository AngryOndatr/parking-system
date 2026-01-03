# API Gateway

This module contains the API Gateway for the parking-system project.

## Test configuration (test profile)

The project uses a test profile to isolate unit and context-loading tests from external systems.
A dedicated `application-test.properties` file is used for the `test` profile under `src/test/resources`.

What it does:
- Disables Flyway migrations during tests (avoids connecting to production Postgres).
- Uses an in-memory H2 datasource so schema is created/dropped quickly during tests.
- Excludes OpenTelemetry auto-configuration to avoid contacting external OTLP exporters in CI/local tests.
- Configures Hibernate to `create-drop` the schema and forces H2 dialect to prevent generation of Postgres-specific SQL.

Why:
- Keeps tests hermetic and fast.
- Avoids flakiness from external dependencies (DB, telemetry endpoints).
- Simplifies CI (no need to run database containers for unit/smoke tests).

Location:
- `src/test/resources/application-test.properties` - contains the above configuration and comments explaining the rationale.

If you need integration tests that run against real Postgres or OTLP, create a separate profile (for example `integration`) and run those tests with Testcontainers or a dedicated environment.

