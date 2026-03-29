# LAFL Spring Platform

Production-style Spring Cloud microservices platform for LAFL logistics workflows.

## Modules

- `gateway` - Spring Cloud Gateway with JWT validation, RBAC edge policy, Redis rate limiter, and health/fallback endpoints
- `config-server` - centralized config profile source (secrets injected at deploy time via `cf set-env`)
- `eureka-server` - service discovery
- `shipment-service` - shipment tracking + status updates + Redis caching
- `user-service` - signup/login, BCrypt verification, JWT issuance
- `quote-service` - quote/contact intake + ops overview/issues APIs
- `notification-service` - dispatches SMTP notifications with a direct demo trigger endpoint

## Database Targets

- Production/Cloud: Supabase PostgreSQL via `DB_URL` secret (recommended: Supabase pooler URL)
- Local development: `docker compose` PostgreSQL service

## Production Integrations

### Kafka (Redpanda, SASL_SSL)

Services using Kafka:

- `shipment-service`
- `user-service`
- `quote-service`
- `notification-service`

Each service reads:

- `KAFKA_BROKERS`
- `KAFKA_SECURITY_PROTOCOL` (recommended `SASL_SSL`)
- `KAFKA_SASL_MECHANISM` (recommended `PLAIN`)
- `KAFKA_SASL_JAAS` (constructed in CI/CD from username/password secrets)

Kafka topics used by the platform:

- `user.registered`
- `quote.submitted`
- `contact.submitted`
- `shipment.status.updated`

CI/CD now bootstraps these topics during deploy (idempotent create/verify) before service startup, so fresh clusters do not fail with `UNKNOWN_TOPIC_OR_PARTITION`.

### Redis (Upstash TLS)

`shipment-service` Redis settings:

- `REDIS_HOST`
- `REDIS_PORT`
- `REDIS_PASSWORD`
- `REDIS_SSL_ENABLED` (set to `true` in CI/CD for Upstash TLS)

### SMTP (Gmail)

`notification-service` SMTP settings:

- `SMTP_HOST` (for Gmail: `smtp.gmail.com`)
- `SMTP_PORT` (for Gmail: `587`)
- `SMTP_USER` (full Gmail address)
- `SMTP_PASS` (Google App Password)
- `MAIL_FROM`
- `MAIL_TO`

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

### Shipment Caching

- `@EnableCaching` enabled
- Tracking lookup cached in `shipments` cache by reference
- Cache evicted on shipment status updates
- TTL configured to 10 minutes via `RedisCacheConfiguration`
- Cloud deploy defaults to `SPRING_CACHE_TYPE=simple` for resilience unless Redis is explicitly configured

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
3. `shipment-service`
4. `user-service`
5. `quote-service`
6. `notification-service`
7. `gateway`

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
3. `smoke-test` (gateway health + tracking endpoint + signup endpoint)

Required GitHub Secrets for deploy:

- `CF_API`
- `CF_USERNAME`
- `CF_PASSWORD`
- `CF_ORG`
- `CF_SPACE`
- `DB_URL`
- `DB_USER`
- `DB_PASS`
- `JWT_SECRET`
- `KAFKA_BROKERS`
- `KAFKA_SECURITY_PROTOCOL`
- `KAFKA_SASL_MECHANISM`
- `KAFKA_USERNAME`
- `KAFKA_PASSWORD`
- `REDIS_HOST`
- `REDIS_PORT`
- `REDIS_PASSWORD`
- `SMTP_HOST`
- `SMTP_PORT`
- `SMTP_USER`
- `SMTP_PASS`
- `MAIL_FROM`
- `MAIL_TO`

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
