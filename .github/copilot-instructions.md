# GitHub Copilot Instructions — MyBlog

> This file configures GitHub Copilot behavior for this repository.
> It is read automatically by Copilot when working in this workspace.
> Think as a **senior engineer building a production-grade system**, not a tutorial project.

---

## Project Identity

- **Name**: MyBlog — a fullstack news/blog platform
- **Backend**: Java 21 + Spring Boot 3.4.3 + Spring Security + JWT + Flyway
- **Frontend**: Angular 10.1.6 + Angular Material 10.2.7 (scheduled for upgrade)
- **Database**: H2 in-memory (to be replaced with PostgreSQL)
- **Deployment target**: Docker + Kubernetes (k3s) on homelab `192.168.0.106`
- **CI/CD**: Jenkins pipelines → build → test → Docker image → push to private registry → deploy to k3s
- **Base package**: `br.com.mekylei.myblog`

---

## General Rules

- Always think about **production impact** — correctness, security, and maintainability first.
- Never generate code that works "just for now." Every suggestion should be production-grade.
- Follow **12-Factor App** principles: config via env vars, stateless processes, explicit dependencies.
- Prefer **explicit over implicit** — annotate, document edge cases, and validate inputs.
- When suggesting a new feature, consider how it will be **containerized and deployed**.
- Read `.github/docs/ai-context.md` and `.github/docs/architecture.md` for full system context before making structural changes.
  The source of truth for these files is `.ia/context/` — if you need to update them, edit there.

---

## Current State Awareness

> **IMPORTANT**: This project is in active refactoring. The rules below define the **target state**.
> The current codebase violates several of them. When generating or modifying code:
>
> - **Do NOT copy existing violations** — they are technical debt to be eliminated, not patterns to follow.
> - **When touching code that violates a rule, fix the violation** as part of the change.
> - **New code must always follow these rules**, regardless of what surrounding code does.
>
> **Active violations to fix when touching related code:**
>
> | File | Violation |
> |------|-----------|
> | `NewsController.java` | Returns `News` entity from create/update/delete/list/topic endpoints |
> | `NewsService.java` | Injects `PagedResourcesAssembler` (web concern in service layer) |
> | `NewsService.java` | `createNews`/`updateNews`/`deleteNews` return `News` entity to controller |
> | `News.java` | Has `@JsonBackReference` annotation and `toFullComment()` mapping method |
> | `Comment.java` | Has `@JsonManagedReference` annotation |
> | `SecurityConfig.java` | `PUT`/`DELETE` matchers use `/news` not `/news/**` — path-variable endpoints are unprotected |
> | `AuthController.java` | Missing `@Valid` on `@RequestBody` parameters |
> | `WebConfig.java` | CORS `allowedOriginPatterns("*")` + `allowCredentials(true)` allows all origins |
> | `NewsDTO`, `FullNewsDTO`, etc. | Naming doesn’t follow the `Request`/`Response` DTO convention |

---

## Backend — Java / Spring Boot

### Package & Layer Structure

Always follow this package layout under `br.com.mekylei.myblog`:

```
controllers/   ← REST endpoints only — no business logic
services/      ← all business logic — one service per domain concept
repositories/  ← Spring Data JPA interfaces only
models/        ← JPA entities (plain persistence model)
dtos/          ← all request/response objects (never expose entities directly)
  ├── news/
  ├── comment/
  ├── auth/
  └── user/
exceptions/    ← custom exceptions + @ControllerAdvice handler
configurations/← Security, CORS, Beans
auth/          ← JWT filter, utility, user details service
enums/         ← domain enums
utils/         ← stateless utility classes only
```

### Controller Rules

```java
// CORRECT: thin controller — delegates immediately to service
@PostMapping
public ResponseEntity<NewsResponseDTO> create(@RequestBody @Valid NewsRequestDTO dto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(newsService.create(dto));
}

// WRONG: business logic inside controller
@PostMapping
public ResponseEntity<News> create(@RequestBody NewsDTO dto) {
    News news = new News();         // ← wrong: entity construction in controller
    news.setTitle(dto.title());
    newsRepository.save(news);      // ← wrong: bypasses service layer
    return ResponseEntity.ok(news); // ← wrong: returns entity directly
}
```

- **Never inject repositories directly into controllers.**
- **Never return JPA entity objects from controllers** — always return a dedicated response DTO.
- Use `@Valid` on all `@RequestBody` parameters.
- Use `ResponseEntity<DTO>` with explicit HTTP status codes.

### Service Rules

```java
// CORRECT: service owns business logic, uses entities internally, returns DTOs
@Service
public class NewsService {

    @Transactional
    public NewsResponseDTO create(NewsRequestDTO dto) {
        validateOwnership(dto);        // business rules here
        News news = dto.toEntity();
        News saved = newsRepository.save(news);
        return NewsResponseDTO.from(saved);  // entity → DTO mapping
    }
}

// WRONG: service returns raw entity to caller
public News create(NewsDTO dto) {
    return newsRepository.save(dto.toNews()); // ← exposes entity to controller
}
```

- One `@Service` class per domain concept (`NewsService`, `CommentService`, `UserService`, `AuthService`).
- All write operations must be `@Transactional`.
- Services must **never** depend on `HttpServletRequest`, `PagedResourcesAssembler`, or any web/presentation layer class. HATEOAS types (`PagedModel`, `EntityModel`) belong in the controller, not the service.
- Return **plain Java types** from service methods: `DTO`, `Page<DTO>`, `List<DTO>`, `Optional<DTO>`. Never `PagedModel`, `EntityModel`, or JPA entities.
- Return DTOs from service methods that are called from controllers.

### DTO Rules

- **Request DTOs**: use Java `record` for immutable input objects. Annotate with `jakarta.validation`.
  ```java
  public record NewsRequestDTO(
      @NotBlank String title,
      @NotBlank @Size(min = 10) String content,
      @NotEmpty List<@NotBlank String> tags
  ) {}
  ```
- **Response DTOs**: use Java `record` with a static factory method:
  ```java
  public record NewsResponseDTO(Long id, String title, String author, LocalDateTime date, List<String> tags) {
      public static NewsResponseDTO from(News news) {
          return new NewsResponseDTO(news.getId(), news.getTitle(), news.getAuthor(), news.getDate(), news.getTags());
      }
  }
  ```
- **Never reuse Request DTOs as Response DTOs.**
- **Never annotate entity classes with Jackson `@JsonIgnore` etc.** to control API output — use DTOs instead.

#### DTO Naming Convention

| Type | Pattern | Example |
|------|---------|----------|
| Request (create/update) | `{Entity}RequestDTO` | `NewsRequestDTO`, `CommentRequestDTO` |
| Response (full detail) | `{Entity}ResponseDTO` | `NewsResponseDTO`, `CommentResponseDTO` |
| Response (list/summary) | `{Entity}ListDTO` | `NewsListDTO` |
| Internal transfer (no HTTP boundary) | No `DTO` suffix | `Token`, `AuthRequest` |

> **Current names to migrate**: `NewsDTO` → `NewsRequestDTO`, `FullNewsDTO` → `NewsResponseDTO`,
> `CommentDTO` → `CommentRequestDTO`, `FullCommentDTO` → `CommentResponseDTO`.

### Entity Rules

- JPA entities live in `models/` package. They represent **database state only** — persistence structure, nothing more.
- **NEVER add `@JsonBackReference`, `@JsonManagedReference`, `@JsonIgnore`, or any Jackson annotation** to entities. Serialization is exclusively controlled by response DTOs.
- **NEVER add mapping or conversion methods** (e.g., `toDTO()`, `toFullComment()`) to entities. Place these as static factory methods on the target DTO:
  ```java
  // WRONG — mapping method on entity:
  public List<FullCommentDTO> toFullComment() { ... }  // in News.java

  // CORRECT — factory method on DTO:
  public static NewsResponseDTO from(News news) { ... }  // in NewsResponseDTO.java
  ```
- `ApiUser` implements `UserDetails` — acceptable for Spring Security integration, but never expose `ApiUser` instances outside the `auth/` package.
- Do not add business logic to entities — keep them as plain field containers with getters/setters.

### Security Rules

- **JWT_SECRET must never be hardcoded.** Always read from environment variable:
  ```yaml
  jwt:
    secret: ${JWT_SECRET}
  ```
- **SecurityConfig matchers MUST use `/**` for endpoints with path variables:**
  ```java
  // CORRECT — matches /news/1, /news/42, etc.:
  .requestMatchers(HttpMethod.POST, "/news").authenticated()
  .requestMatchers(HttpMethod.PUT, "/news/**").authenticated()
  .requestMatchers(HttpMethod.DELETE, "/news/**").authenticated()

  // WRONG — only matches the literal path /news, not /news/{id}:
  .requestMatchers(HttpMethod.PUT, "/news").authenticated()
  ```
- **Never use `allowedOriginPatterns("*")` with `allowCredentials(true)`** in production:
  ```java
  // CORRECT: restrict to known frontend origin per environment
  registry.addMapping("/**")
      .allowedOriginPatterns("https://myblog.homelab.local")
      .allowCredentials(true);
  // Use Spring profiles (application-prod.yml) to apply different CORS config per environment
  ```
- Never expose the H2 console outside local dev profiles. Use Spring profiles to gate it:
  ```yaml
  # application-prod.yml
  spring:
    h2:
      console:
        enabled: false
  ```
- Every write endpoint (`POST`, `PUT`, `DELETE`) on business resources must require authentication.
  Current gaps: `POST /news` unprotected; `PUT /news/**` and `DELETE /news/**` matchers are broken.
- Use `@PreAuthorize` for method-level security when ownership checks are needed:
  ```java
  @PreAuthorize("@newsAuthorizationService.isOwner(#id, authentication.name)")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) { ... }
  ```
- **Always add `@Valid`** on all `@RequestBody` parameters — including auth endpoints.
- Never catch bare `Exception` in security or JWT code — catch specific exceptions (`JwtException`, `IllegalArgumentException`) and log them. Silent failures hide attacks.
- CSRF is disabled (acceptable for stateless JWT APIs) — document this with a comment in `SecurityConfig`.

### Error Handling

All errors must flow through `ApiExceptionHandler` (`@ControllerAdvice`). The `ErrorResponse` record format:

```json
{
  "timestamp": "2026-04-16T10:00:00Z",
  "status": 404,
  "error": "News not found",
  "message": "News with id 42 not found",
  "path": "/news/42"
}
```

- Create specific exception classes for each domain error: `NewsNotFoundException`, `CommentNotFoundException`, `UserNotFoundException`, `TokenException`.
- Map HTTP statuses semantically: `404` for not found, `400` for validation failure, `401` for auth failure, `403` for authorization failure, `409` for conflict, `422` for business rule violation.
- Never return stack traces to the client.

### Validation

- Always use `@Valid` on controller `@RequestBody` — this triggers `jakarta.validation` constraints.
- Catch `MethodArgumentNotValidException` in `ApiExceptionHandler` and return a structured 400 response listing all field errors.
- For business-level validation (e.g., "only the author can edit"), throw a custom exception from the service layer.

### Configuration & Environment

- All configuration that varies between environments must use environment variables.
- Add `application-prod.yml` with PostgreSQL settings and strict security config.
- Add `application-dev.yml` for local dev shortcuts.
- Never commit secrets to version control — reference `.env` files or CI/CD credentials.
- When adding a new configurable value, always add a `${VARIABLE_NAME:defaultValue}` fallback:
  ```yaml
  jwt:
    secret: ${JWT_SECRET}
    access-token-expiry: ${JWT_ACCESS_TOKEN_EXPIRY:900}
    refresh-token-expiry: ${JWT_REFRESH_TOKEN_EXPIRY:604800}
  ```

### Testing

- Every new `@Service` method must have a corresponding unit test in `src/test/java/`.
- Use `@ExtendWith(MockitoExtension.class)` for unit tests — mock repositories and dependencies.
- Use `@SpringBootTest` + `@AutoConfigureMockMvc` for integration tests.
- Test naming: `methodName_whenCondition_shouldExpectedBehavior`:
  ```java
  @Test
  void createNews_whenTitleIsBlank_shouldThrowValidationException() { ... }
  ```
- Always test: happy path, validation failure, not-found case, unauthorized access.

---

## Frontend — Angular

### Service Layer

Every interaction with the backend must go through a typed Angular service. Never make `HttpClient`
calls directly from components.

```typescript
// CORRECT: typed service with Observable
@Injectable({ providedIn: 'root' })
export class NewsService {
  private readonly api = `${environment.apiURL}/news`;

  constructor(private http: HttpClient) {}

  getNews(page: number, size: number): Observable<NewsPageable> {
    return this.http.get<NewsPageable>(`${this.api}?page=${page}&size=${size}`);
  }
}

// WRONG: HttpClient in component
export class NewsListComponent {
  constructor(private http: HttpClient) {  // ← never inject HttpClient in components
    this.http.get('/news').subscribe(...);
  }
}
```

### Component Architecture

- Follow **smart vs. dumb component** pattern:
  - **Smart (container) components**: inject services, manage state, pass data via `@Input`
  - **Dumb (presentational) components**: receive data via `@Input`, emit events via `@Output`
- Keep components focused: one component = one responsibility.
- Never put business logic in components — extract to services.

### Interface Contracts

Frontend interfaces in `shared/interfaces/` must match **API response DTOs**, not backend entities.

```typescript
// DO: interface matches API contract
export interface NewsResponse {
  id: number;
  title: string;
  author: string;
  date: string;       // ISO string from API
  tags: string[];
}

// DO NOT: copy Java entity structure with JPA annotations hints
```

When the backend response format changes, update these interfaces — they are the frontend API contract.

### Authentication

- Tokens are stored in `sessionStorage` — this is intentional (reduced XSS attack surface vs localStorage).
- Token refresh is handled automatically by `AuthService` — do not manually refresh tokens in components.
- `JwtInterceptor` attaches the Authorization header — do not manually set headers on authenticated requests.
- Check authentication state via `AuthService.isAuthenticated$` (Observable) — bind to this in templates.

### Environment Configuration

- **Development**: `apiURL: 'http://localhost:8080'` in `environment.ts` — direct local API.
- **Production / Docker / Kubernetes**: `apiURL: ''` (empty string — all API calls use relative paths).
- **The Nginx reverse proxy approach is mandatory for non-local environments:**
  ```nginx
  # nginx-custom.conf — required in the client container for Docker and Kubernetes
  location /api/ {
      proxy_pass http://myblog-api-svc:8080/;
      proxy_set_header Host $host;
      proxy_set_header X-Real-IP $remote_addr;
      proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
  }
  ```
  Angular services call `/api/news`, `/api/auth/login`, etc. Nginx forwards to the backend.
  This eliminates CORS entirely and works identically in Docker and Kubernetes.
- **NEVER use `http://127.0.0.1:8080` or `http://myblog-api-svc:8080` as the Angular API URL.**
  The first only works when browser and server are on the same machine.
  The second is a cluster-internal DNS name not resolvable from a browser.
- API URL must always come from `environment.ts` / `environment.prod.ts` — never hardcode in components or services.

### Angular Upgrade Guidance

This project is on Angular 10. Upgrade incrementally, one major version at a time.

- Before each upgrade, run `ng update @angular/core @angular/cli --next` and review breaking changes.
- Update Angular Material alongside Angular core — versions must match.
- Replace `tslint` with `@angular-eslint` on first upgrade (Angular 12+).
- Target long-term: standalone components, Angular Signals (v16+), new control flow syntax (v17+).
- Update Node in Docker from 14 → 20 LTS.

### Module Structure

- Keep lazy-loaded modules for feature areas (`NewsModule`, `UserModule`).
- `SharedModule` exports reusable components/pipes/directives only — no services.
- `CoreModule` provides singleton services and one-time components (Header, Footer) — import only in `AppModule`.

---

## Docker & Containerization

### General Rules

- Every Dockerfile must use **multi-stage builds** to minimize final image size.
- Final images must never contain build tools (JDK, Node, Maven, npm).
- Use specific version tags — never `latest` in production Dockerfiles:
  ```dockerfile
  FROM eclipse-temurin:21-jre-jammy   # ✓
  FROM eclipse-temurin:latest         # ✗
  ```
- Never bake secrets into Docker images. Use `ENV` for secret references only, not values:
  ```dockerfile
  # WRONG (current state — fix this):
  ENV JWT_SECRET="gqUrQCpx4KT4Q9Zig5lcDyVVTH023MZ/cJcFseu77PU="

  # CORRECT: inject at runtime
  # docker run -e JWT_SECRET=$SECRET ... or via K8s Secret
  ```

### API Dockerfile Best Practices

```dockerfile
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app
COPY gradlew build.gradle settings.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon  # cache dependencies layer
COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
# No ENV JWT_SECRET here — inject at runtime
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Client Dockerfile Best Practices

```dockerfile
FROM node:20-alpine AS builder   # upgrade from node:14
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci --prefer-offline
COPY . .
ARG API_BASE_URL=http://localhost:8080
RUN npm run build -- --configuration=production

FROM nginx:1.25-alpine
COPY --from=builder /app/dist/myblog /usr/share/nginx/html
COPY config/nginx-custom.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
ENTRYPOINT ["nginx", "-g", "daemon off;"]
```

### Kubernetes Compatibility

All containers must be:
- **Stateless** — no local file writes for persistent data.
- **Health-check ready** — expose `/actuator/health` (backend) or return `200` on `/` (client).
- **ENV-configurable** — all config via environment variables.
- **Graceful shutdown aware** — Spring Boot handles this with `server.shutdown: graceful`.

---

## CI/CD — Jenkins Pipeline Rules

- Every `Jenkinsfile` must be **declarative pipeline** format.
- Secrets (JWT secret, registry credentials) must reference **Jenkins credentials** — never inline:
  ```groovy
  environment {
      JWT_SECRET = credentials('myblog-jwt-secret')
  }
  ```
- All stages must handle failures gracefully — use `post { failure { ... } }` blocks.
- Build version must be traceable: tag images with `${env.BUILD_NUMBER}` or git tag.
- Tests must run before Docker build — never push a broken image.
- After deployment, verify with `kubectl rollout status` before marking pipeline as success.
- Pipeline stages must be: `Checkout → Build → Test → Docker Build → Push → Deploy → Verify`.

---

## Kubernetes (k3s) Rules

- All manifests belong in a `k8s/` directory at repo root (create it when adding K8s support).
- Use `Namespace: myblog` to isolate all resources.
- Secrets must use `kind: Secret` — never store secret values in ConfigMaps.
- All Deployments must define `readinessProbe` and `livenessProbe`.
- Resource requests and limits must be defined on all containers.
- Use `imagePullPolicy: Always` for non-versioned tags; `IfNotPresent` for versioned tags.
- Private registry: `192.168.0.106:5000` — configure `imagePullSecrets` if registry auth is enabled.

---

## DO NOT Rules

These are hard rules that must never be violated:

| Rule | Why |
|------|-----|
| Do NOT return JPA entities from API endpoints | Breaks encapsulation, exposes internals, makes refactoring dangerous |
| Do NOT put business logic in controllers | Untestable, violates SRP |
| Do NOT put business logic in Angular components | Untestable, not reusable |
| Do NOT hardcode secrets or URLs | Security vulnerability, environment coupling |
| Do NOT use H2 for anything other than local dev | Data loss on restart, not production-safe |
| Do NOT catch bare `Exception` to suppress errors silently | Hides bugs, prevents debugging |
| Do NOT break existing API contracts without versioning | Breaks frontend and external consumers |
| Do NOT inject HttpClient directly in Angular components | Bypasses the service layer |
| Do NOT commit `.env` files or credentials | Security violation |
| Do NOT bake `JWT_SECRET` into Docker images | Secret visible in image history |
| Do NOT skip `@Transactional` on write operations | Risk of partial updates and data corruption |
| Do NOT use `FetchType.EAGER` on large collections without pagination | N+1 queries, OOM risk |
| Do NOT use SecurityConfig matchers without `/**` for path-variable endpoints | Endpoints fall through to `permitAll()` — silent security bypass |
| Do NOT use `allowedOriginPatterns("*")` with `allowCredentials(true)` in production | Allows any website to make credentialed API requests |
| Do NOT add Jackson annotations (`@JsonBackReference`, `@JsonManagedReference`) to JPA entities | Mixes serialization with persistence; breaks DTO-first design |
| Do NOT add mapping/conversion methods to JPA entities | Violates SRP; creates bidirectional DTO↔entity coupling |
| Do NOT return Spring HATEOAS types (`PagedModel`, `EntityModel`) from service methods | Couples business layer to web presentation framework |
| Do NOT use `npm install` in Dockerfiles — use `npm ci` | Non-deterministic builds; `npm ci` guarantees lockfile fidelity |

---

## Modernization Checklist

When working on features, always check:

- [ ] Is the JWT_SECRET externalized (NOT in Dockerfile)?
- [ ] Is H2 console disabled for non-dev profiles?
- [ ] Does the endpoint return a DTO, not an entity?
- [ ] Is all new business logic in a `@Service` class?
- [ ] Are there unit tests for the new service method?
- [ ] Does the Angular component delegate HTTP calls to a service?
- [ ] Are environment-specific values in `environment.ts` / `application.yml` (not hardcoded)?
- [ ] Is the code compatible with Docker and Kubernetes deployment?
- [ ] Would this work with multiple running pod replicas (statelessness check)?
- [ ] Is the CI/CD pipeline still valid after this change?
- [ ] Do `SecurityConfig` matchers for path-variable routes use `/**` (not bare path)?
- [ ] Is CORS restricted to known origins (not `allowedOriginPatterns("*")`) in the production profile?
- [ ] Does the service return DTOs or `Page<DTO>` — not `PagedModel`, `EntityModel`, or raw entities?
- [ ] Are JPA entities free of Jackson annotations and DTO mapping methods?
- [ ] Does `environment.prod.ts` use empty `apiURL: ''` so all API calls route through Nginx?
