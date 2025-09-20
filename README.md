
# auth-service-spring-gradle

Spring Boot Auth Service skeleton (Gradle build) — simplified MVP for registration/login/refresh.

## Quick start (dev)

1. Build:
   ```
   ./gradlew clean build
   ```

2. Start local infra:
   ```
   docker compose up --build
   ```

3. The app will be at http://localhost:8080

Endpoints:
- POST /api/v1/auth/register
- POST /api/v1/auth/login
- POST /api/v1/auth/logout

Notes:
- RSA keys and secrets: configure via Vault/KMS in prod.
- This skeleton is intentionally minimal; add validations, exception handlers, tests, and integrations.
