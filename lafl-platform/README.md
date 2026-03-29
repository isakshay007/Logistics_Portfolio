# LAFL Spring Platform

Production-style Spring Cloud microservices platform for LAFL logistics workflows.

## Modules

- `gateway` - Spring Cloud Gateway with JWT validation, RBAC edge policy, Redis rate limiter, and health/fallback endpoints
- `config-server` - centralized config with shared overrides (`jwt.secret`, Redis host/port)
- `eureka-server` - service discovery
- `shipment-service` - shipment tracking + status updates + Redis caching
- `user-service` - signup/login, BCrypt verification, JWT issuance
- `quote-service` - quote/contact intake + ops overview/issues APIs
- `notification-service` - dispatches SMTP notifications with a direct demo trigger endpoint

## Database Targets

- Production/Cloud: Supabase PostgreSQL (`jdbc:postgresql://db.epvdfxwkqdaaeornsotb.supabase.co:5432/postgres`)
- Local development: `docker compose` PostgreSQL service

## Implemented Platform Features

### Auth + RBAC at Gateway

- Validates `Authorization: Bearer <token>` using `jwt.secret` from Config Server
- Forwards identity headers downstream:
  - `X-User-Id`
  - `X-User-Role`
- Route access model:
  - Public:
    - `GET /api/v1/shipments/track`
    - `POST /api/v1/quotes/**`
    - `POST /api/v1/contacts/**`
    - `POST /api/v1/auth/**`
  - JWT required:
    - `POST /api/v1/shipments/**`
  - `ROLE_OPS` required:
    - `GET /api/v1/ops/**`

### Notifications (Direct Trigger)

- `notification-service` exposes `POST /api/v1/notifications/trigger`
- Accepts `eventType` + `payload` and routes directly to notification dispatch logic
- Event payloads are JSON POJOs with `eventType`, `timestamp`, and event-specific fields

### Redis Caching (Shipment)

- `@EnableCaching` enabled
- Tracking lookup cached in `shipments` cache by reference
- Cache evicted on shipment status updates
- TTL configured to 10 minutes via `RedisCacheConfiguration`

### OpenAPI / Swagger

Enabled on:

- `shipment-service`
- `user-service`
- `quote-service`

Per-service endpoints:

- Swagger UI: `/swagger-ui.html`
- OpenAPI JSON: `/v3/api-docs`

Bearer JWT security scheme is registered so protected APIs can be tested directly in Swagger UI.

## Local Development (One Command)

Run full platform (infra + all services):

```bash
docker compose up --build
```

Key endpoints:

- Gateway: `http://localhost:8080`
- Config Server: `http://localhost:8888`
- Eureka: `http://localhost:8761`
- MailHog UI: `http://localhost:8025`

## Cloud Foundry

Each module has `manifest.yml` with:

- `memory: 512M`
- `instances: 1`
- `CF_APP_NAME` matching the module name

Startup order for deployment:

1. `config-server`
2. `eureka-server`
3. `gateway`
4. `shipment-service`
5. `user-service`
6. `quote-service`
7. `notification-service`

Flyway schema note for Supabase:

- `shipment-service`: `spring.flyway.schemas=shipment_service`
- `user-service`: `spring.flyway.schemas=user_service`
- `quote-service`: `spring.flyway.schemas=quote_service`
- These schemas must exist in Supabase before migrations run.

## CI/CD

Workflow file:

- `.github/workflows/lafl-platform-ci.yml`

Jobs:

1. `build-and-test` (Gradle test across modules)
2. `deploy` (Cloud Foundry push in startup order)
3. `smoke-test` (`GET /api/health` via gateway)

## Testing

Run all module tests:

```bash
gradle test
```

Test coverage includes:

- Gateway JWT/RBAC behavior (401/403/200 scenarios)
- User login success/failure behavior
- Shipment cache hit/eviction behavior
- OpenAPI docs endpoint tests for shipment/user/quote services
