# GitHub Copilot Instructions — MyBlog

> Behavioral rules for AI agents. Read BEFORE generating any code.

---

## First Steps — ALWAYS

Before making any code change or architectural decision:

1. Read `.github/docs/ai-context.md` — domain model, API surface, business rules
2. Read `.github/docs/architecture.md` — system structure, known problems, deployment
3. Then follow the rules below

---

## Project Summary

Java 21 + Spring Boot 3.4.3 + Angular 10.1.6 | Base package: `br.com.mekylei.myblog`
Full stack details in `ai-context.md`. Architecture in `architecture.md`.

---

## General Rules

- Think **production impact** — correctness, security, maintainability first
- Follow **12-Factor App** principles
- Prefer **explicit over implicit** — annotate, validate inputs, document edge cases
- Consider containerization and deployment for every feature

---

## Current State Awareness

> This project is in active refactoring. Rules below define the **target state**.
> **Do NOT copy existing violations** — fix them when touching related code.

**Active violations to fix when touching related code:**

| File | Violation |
|------|-----------|
| `NewsController.java` | Returns `News` entity from create/update/delete/list/topic |
| `NewsService.java` | Injects `PagedResourcesAssembler` (web concern in service) |
| `NewsService.java` | create/update/delete return `News` entity to controller |
| `News.java` | `@JsonBackReference` + `toFullComment()` mapping method |
| `Comment.java` | `@JsonManagedReference` annotation |
| `SecurityConfig.java` | `PUT`/`DELETE` use `/news` not `/news/**` — endpoints unprotected |
| `AuthController.java` | Missing `@Valid` on `@RequestBody` |
| `WebConfig.java` | CORS `allowedOriginPatterns("*")` + `allowCredentials(true)` |
| DTOs | `NewsDTO`/`FullNewsDTO`/`CommentDTO`/`FullCommentDTO` — rename to Request/Response convention |

---

## Backend Rules

### Controllers

- Thin controllers — delegate immediately to service
- Never inject repositories into controllers
- **Never return JPA entities** — always return response DTOs
- `@Valid` on all `@RequestBody` parameters
- `ResponseEntity<DTO>` with explicit HTTP status

### Services

- One `@Service` per domain concept
- `@Transactional` on all write operations
- **Never** depend on `HttpServletRequest`, `PagedResourcesAssembler`, or web-layer classes
- Return DTOs, `Page<DTO>`, `List<DTO>` — never entities, `PagedModel`, or `EntityModel`

### DTOs

- **Request DTOs**: Java `record` with `jakarta.validation` annotations
- **Response DTOs**: Java `record` with `static from(Entity)` factory method
- Never reuse request DTO as response DTO

**Naming convention:**

| Type | Pattern | Example |
|------|---------|---------|
| Request | `{Entity}RequestDTO` | `NewsRequestDTO` |
| Response (detail) | `{Entity}ResponseDTO` | `NewsResponseDTO` |
| Response (list) | `{Entity}ListDTO` | `NewsListDTO` |
| Internal | No `DTO` suffix | `Token`, `AuthRequest` |

### Entities

- JPA entities in `models/` — database state only
- **No Jackson annotations** (`@JsonIgnore`, `@JsonBackReference`, etc.)
- **No mapping methods** (`toDTO()`, `toFullComment()`) — put `from()` on the DTO
- No business logic in entities

### Security

- `JWT_SECRET` from env var only: `${JWT_SECRET}`
- SecurityConfig matchers **must use `/**`** for path-variable endpoints
- **Never** `allowedOriginPatterns("*")` + `allowCredentials(true)` in production
- Every write endpoint (`POST`/`PUT`/`DELETE`) on business resources must require auth
- `@Valid` on all `@RequestBody` — including auth endpoints
- Never catch bare `Exception` in JWT/security code — catch specific exceptions and log

### Error Handling

- All errors through `ApiExceptionHandler` (`@ControllerAdvice`)
- Specific exception classes per domain error
- Semantic HTTP statuses: 404 not found, 400 validation, 401 auth, 403 authz, 409 conflict
- Never return stack traces to client

### Testing

- Every `@Service` method needs a unit test
- `@ExtendWith(MockitoExtension.class)` for unit tests
- `@SpringBootTest` + `@AutoConfigureMockMvc` for integration
- Naming: `methodName_whenCondition_shouldExpectedBehavior`
- Test: happy path, validation failure, not-found, unauthorized

### Configuration

- All env-varying config via environment variables with fallbacks: `${VAR:default}`
- Never commit secrets; gate H2 console behind dev profile

---

## Frontend Rules

### Services

- All HTTP calls through typed Angular services — never `HttpClient` in components
- API URL from `environment.ts` / `environment.prod.ts` — never hardcode

### Components

- Smart (container) components inject services, manage state
- Dumb (presentational) components use `@Input`/`@Output` only
- No business logic in components

### Interfaces

- Must match API response DTOs, not backend entities

### Environment

- Dev: `apiURL: 'http://localhost:8080'`
- Prod/Docker/K8s: `apiURL: ''` (empty — relative paths through Nginx proxy)
- **Never** use `http://127.0.0.1:8080` or `http://myblog-api-svc:8080` as Angular API URL

---

## Docker Rules

- Multi-stage builds; final image must not contain build tools
- Specific version tags — never `latest` in production Dockerfiles
- **Never bake secrets** into images
- Use `npm ci` (not `npm install`) in Dockerfiles

---

## Deployment Rules

- K8s manifests in `k8s/` directory, namespace `myblog`
- Secrets via `kind: Secret` — never in ConfigMaps
- All Deployments need `readinessProbe` + `livenessProbe` + resource limits
- Jenkins pipelines: declarative format, secrets from credentials, tests before Docker build

---

## DO NOT Rules

| Rule | Why |
|------|-----|
| Return JPA entities from endpoints | Exposes internals, dangerous for refactoring |
| Business logic in controllers | Untestable, violates SRP |
| Business logic in Angular components | Untestable, not reusable |
| Hardcode secrets or URLs | Security + environment coupling |
| Catch bare `Exception` silently | Hides bugs and attacks |
| Skip `@Transactional` on writes | Partial update risk |
| `FetchType.EAGER` on large collections without pagination | N+1 + OOM risk |
| SecurityConfig matchers without `/**` for path variables | Silent security bypass |
| `allowedOriginPatterns("*")` + `allowCredentials(true)` | Any origin with credentials |
| Jackson annotations on JPA entities | Mixes serialization with persistence |
| Mapping methods on JPA entities | Bidirectional DTO↔entity coupling |
| HATEOAS types from service methods | Couples business to web framework |
| `npm install` in Dockerfiles | Non-deterministic builds |
| `HttpClient` in Angular components | Bypasses service layer |
| Bake `JWT_SECRET` into Docker images | Secret in image history |

---

## Modernization Checklist

When working on any feature, verify:

- [ ] JWT_SECRET externalized (not in Dockerfile)?
- [ ] H2 console disabled for non-dev profiles?
- [ ] Endpoint returns DTO, not entity?
- [ ] Business logic in `@Service` class?
- [ ] Unit tests for new service methods?
- [ ] Angular HTTP calls through services?
- [ ] Env-specific values in config files (not hardcoded)?
- [ ] Compatible with Docker + K8s deployment?
- [ ] Stateless (works with multiple replicas)?
- [ ] SecurityConfig matchers use `/**` for path variables?
- [ ] CORS restricted in production profile?
- [ ] Service returns DTOs / `Page<DTO>` (not entities, not HATEOAS types)?
- [ ] Entities free of Jackson annotations and mapping methods?
- [ ] `environment.prod.ts` uses `apiURL: ''`?
