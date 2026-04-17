> ⚠️ SOURCE OF TRUTH — Edit here. Sync proxy after changes.
> Proxy: `.github/docs/architecture.md`

# Architecture — MyBlog

> System structure, deployment topology, known problems, and roadmap.
> For domain model and API surface: see `ai-context.md`.
> For coding rules: see `.github/copilot-instructions.md`.

---

## Table of Contents

1. [Current Architecture](#1-current-architecture)
2. [Runtime Architecture](#2-runtime-architecture)
3. [Deployment Architecture](#3-deployment-architecture)
4. [Data Flow](#4-data-flow)
5. [Known Problems](#5-known-problems)
6. [Improvement Roadmap](#6-improvement-roadmap)
7. [DevOps & Infra](#7-devops--infra)

---

## 1. Current Architecture

### 1.1 Monorepo Structure

```
myblog/
├── api/                         ← Spring Boot backend
│   ├── src/main/java/br/com/mekylei/myblog/
│   │   ├── auth/                ← JWT filter + utility
│   │   ├── configurations/      ← SecurityConfig, BeanConfig, WebConfig
│   │   ├── controllers/         ← REST controllers
│   │   ├── dtos/                ← Request/Response DTOs
│   │   ├── enums/               ← NotFoundBy, etc.
│   │   ├── exceptions/          ← ApiExceptionHandler + custom exceptions
│   │   ├── models/              ← JPA entities
│   │   ├── repositories/        ← Spring Data JPA interfaces
│   │   ├── services/            ← Business logic
│   │   └── utils/               ← DateUtil
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/specific/h2/      ← Flyway migrations
│   └── Dockerfile               ← Multi-stage (JDK → JRE)
│
├── client/                      ← Angular frontend
│   ├── src/app/
│   │   ├── auth/                ← AuthService, JwtInterceptor
│   │   ├── core/                ← Header, Footer
│   │   ├── home/                ← Home module
│   │   ├── news/                ← News module (lazy loaded)
│   │   │   ├── news-list/
│   │   │   ├── news-details/
│   │   │   └── news-search/
│   │   └── shared/              ← Interfaces, services, components, utils
│   ├── src/environments/
│   ├── config/nginx-custom.conf
│   └── Dockerfile               ← Multi-stage (Node 14 → nginx:alpine)
│
├── docker-compose.yml
└── docker-compose-windows.yml
```

### 1.2 Backend Layered Architecture

```
HTTP Request
    │
    ▼
JwtFilter (OncePerRequestFilter)     ← validates Bearer token
    │
    ▼
SecurityFilterChain                  ← authorization rules
    │
    ▼
Controller Layer                     ← input validation, HTTP concerns
    │  DTOs
    ▼
Service Layer                        ← business logic, transactions
    │  Entities
    ▼
Repository Layer (Spring Data JPA)   ← database queries
    │
    ▼
H2 In-Memory Database                ← schema managed by Flyway
```

#### Current Architecture Violations

| Violation | Location | Impact |
|-----------|----------|--------|
| Controller returns `News` entity from create/update/delete/list/topic | NewsController.java | Exposes JPA internals to API consumers |
| Service injects `PagedResourcesAssembler` (web/presentation class) | NewsService.java | Couples service layer to presentation |
| Entity performs DTO mapping (`toFullComment()`) | News.java | Mapping logic on persistence object |
| Jackson annotations on entities (`@JsonBackReference`/`@JsonManagedReference`) | News.java, Comment.java | Entities control serialization |
| `AuthResponse` DTO imports `JwtUtil.ACCESS_TOKEN_EXPIRES_IN` | AuthResponse.java | DTO depends on auth infrastructure |

### 1.3 Frontend Module Structure

```
AppModule
├── AppRoutingModule        ← root routing
├── HomeModule              ← /home
├── NewsModule (lazy)       ← /postagem
│   ├── NewsListComponent   ← paginated list
│   ├── NewsDetailsComponent← detail + comments
│   └── NewsSearchComponent ← search bar
├── SharedModule            ← interfaces, components, services, utils
└── CoreModule
    ├── HeaderModule        ← nav bar + auth controls
    └── FooterModule
```

Key services: `NewsService` (HTTP CRUD), `AuthService` (JWT lifecycle), `JwtInterceptor` (token attachment).

---

## 2. Runtime Architecture

### 2.1 Current (docker-compose)

```
┌──────────────────────────────────────────────────┐
│              Docker Host (Linux)                 │
│                                                  │
│  ┌───────────────────┐  ┌───────────────────┐    │
│  │  client-myblog    │  │  api-myblog       │    │
│  │  nginx:alpine     │  │  temurin:21-jre   │    │
│  │  port 8082:80     │  │  port 8080:8080   │    │
│  │  Angular SPA      │  │  Spring Boot      │    │
│  │  (static files)   │  │  + H2 in-memory   │    │
│  └────────┬──────────┘  └───────────────────┘    │
│           │ serves              ▲                │
└───────────┼─────────────────────┼────────────────┘
            │                     │
         Browser ─── XHR ─────────┘
        (port 8082)         (port 8080)
```

- Angular built with `API_BASE_URL=http://127.0.0.1:8080` — resolves in browser, not in container
- H2 is embedded in JVM — no persistence between restarts
- `SPRING_PROFILES_ACTIVE: prod` set in compose but no `application-prod.yml` exists yet

### 2.2 Ports

| Service       | Container | Host | Protocol |
|---------------|-----------|------|----------|
| api-myblog    | 8080      | 8080 | HTTP     |
| client-myblog | 80        | 8082 | HTTP     |
| H2 console    | 8080      | 8080 | HTTP     |

---

## 3. Deployment Architecture

### 3.1 Homelab Context

```
Homelab Server: 192.168.0.106
├── Docker (standalone + compose)
├── Private Docker Registry (192.168.0.106:5000)
├── k3s (Kubernetes)
└── Jenkins (CI/CD)
```

### 3.2 Target Kubernetes Architecture (k3s)

```
k3s Cluster (192.168.0.106)
│
├── Namespace: myblog
│   ├── Deployment: myblog-api (2 replicas)
│   │   └── image: 192.168.0.106:5000/myblog-api:<version>
│   │       port 8080, envFrom: Secret/myblog-api-secrets
│   │
│   ├── Service: myblog-api-svc (ClusterIP :8080)
│   │
│   ├── Deployment: myblog-client (2 replicas)
│   │   └── image: 192.168.0.106:5000/myblog-client:<version>
│   │       port 80
│   │
│   ├── Service: myblog-client-svc (ClusterIP :80)
│   │
│   ├── Ingress: myblog-ingress (Traefik)
│   │   ├── /     → myblog-client-svc:80
│   │   └── /api/ → myblog-api-svc:8080
│   │
│   └── PostgreSQL StatefulSet + PVC (future)
```

> K8s manifests live in `k8s/` at repo root. See copilot-instructions.md for manifest rules.

### 3.3 Environment Variables

| Variable                | Where            | Injection Method                |
|-------------------------|------------------|---------------------------------|
| `JWT_SECRET`            | api container    | Kubernetes Secret               |
| `SPRING_DATASOURCE_URL` | api container    | Kubernetes ConfigMap            |
| `API_BASE_URL`          | client build arg | Build-time ARG (empty for prod) |

### 3.4 Spring Boot Profiles

| Profile   | Database     | H2 Console | JWT Secret | Log Level |
|-----------|--------------|------------|------------|-----------|
| `default` | H2 in-memory | Enabled    | Env var    | DEBUG     |
| `prod`    | PostgreSQL   | Disabled   | K8s Secret | INFO      |

```
api/src/main/resources/
├── application.yml          ← shared defaults
├── application-dev.yml      ← local dev overrides (H2 console, debug logging, permissive CORS)
└── application-prod.yml     ← production config (PostgreSQL, H2 disabled, restricted CORS, INFO logging)
```

---

## 4. Data Flow

### 4.1 Public Read (GET /news)

```
Browser → Nginx (serves SPA) → Angular NewsService
  → GET http://[host]:8080/news?page=0&size=5
  → JwtFilter (no token → skip)
  → SecurityFilterChain (permitAll)
  → NewsController.getNews(pageable)
  → NewsService.getNews(pageable) [sort: id DESC]
  → NewsRepository.findAll(pageable) → H2
  → Page<News> → pagedResourcesAssembler.toModel()
  → HAL+JSON response
  → Angular maps to NewsPageable → renders
```

### 4.2 Authentication (JWT Lifecycle)

```
POST /auth/login { email, password }
  → AuthService.login()
  → CustomUserDetailsService.loadByUsername(email)
  → BCrypt.matches(raw, hashed)
  → JwtUtil.generateAccessToken(email)    [HS256, 15min]
  → JwtUtil.generateRefreshToken(email)   [HS256, 7 days]
  → RefreshTokenRepository.save()
  → Returns { accessToken, refreshToken }

Angular AuthService:
  → stores in sessionStorage
  → schedules refresh at (expiry - 5min)

Subsequent requests:
  → JwtInterceptor adds Authorization: Bearer <token>
  → JwtFilter validates signature + expiry
  → extracts email → loads UserDetails → sets SecurityContext

Token refresh:
  POST /auth/refresh { refreshToken }
  → validates from DB → issues new pair → deletes old token
```

---

## 5. Known Problems

### 5.1 Security

| # | Issue | Severity | Location |
|---|-------|----------|----------|
| 1 | `PUT`/`DELETE` matchers use `/news` not `/news/**` — path-variable endpoints unprotected | **CRITICAL** | SecurityConfig.java |
| 2 | `POST /news` not protected — anyone can create news | **CRITICAL** | SecurityConfig.java |
| 3 | `JWT_SECRET` hardcoded in Dockerfile `ENV` — visible in image history | **CRITICAL** | api/Dockerfile |
| 4 | H2 console with `web-allow-others: true` — DB browsable from network | **HIGH** | application.yml |
| 5 | CORS: `allowedOriginPatterns("*")` + `allowCredentials(true)` | **HIGH** | WebConfig.java |
| 6 | No HTTPS — credentials in plaintext | **HIGH** | — |
| 7 | `JwtUtil.validateToken()` catches bare `Exception` — silent failure | **MEDIUM** | JwtUtil.java |
| 8 | No rate limiting on `/auth/login` | **MEDIUM** | — |
| 9 | Missing `@Valid` on AuthController endpoints | **LOW** | AuthController.java |
| 10 | CSRF disabled — intentional for JWT, but undocumented | **INFO** | SecurityConfig.java |

### 5.2 Data & Persistence

- H2 in-memory — all data lost on restart
- Flyway scripts are H2-specific (dialect differences for PostgreSQL migration)
- `News.author` and `Comment.author` are plain strings — no FK to `ApiUser`
- Tags are free-form strings with no deduplication

### 5.3 API Design

- `NewsController` returns `News` entity directly (not DTOs) in POST/PUT/DELETE responses
- No ownership validation — any authenticated user can overwrite any author field
- No API versioning, no standard response envelope
- Mixed use of raw entity, DTO, and HAL responses

### 5.4 Frontend

- Angular 10 is 6+ major versions behind current; known CVEs in dependencies
- Node 14 in Docker is EOL since 2023
- `API_BASE_URL=http://127.0.0.1:8080` works only when browser and API are on same machine
- `Post.id` typed as `string` in Angular but backend returns `number` — silent type coercion

### 5.5 Tight Coupling

- `News`/`Comment` entities have Jackson annotations (`@JsonBackReference`/`@JsonManagedReference`)
- `NewsService` depends on `PagedResourcesAssembler<News>` (web-layer class)
- `AuthResponse` DTO references `JwtUtil.ACCESS_TOKEN_EXPIRES_IN` at compile time
- Frontend interfaces mirror entity shapes, not API contracts

### 5.6 Scalability

- H2 cannot be shared across replicas
- No caching layer
- No async processing

---

## 6. Improvement Roadmap

### Phase 1 — Security & Stability

1. Extract `JWT_SECRET` from Dockerfile → env var / K8s Secret
2. Fix SecurityConfig: `PUT`/`DELETE` matchers → `/news/**`; protect `POST /news`
3. Disable H2 console in non-dev profiles
4. Create `application-prod.yml` (H2 disabled, PostgreSQL placeholder)
5. Lock down CORS to known origins in production
6. Fix `API_BASE_URL` → empty string + Nginx reverse proxy

### Phase 2 — Data Layer Migration

1. Replace H2 with PostgreSQL (containerized + k8s StatefulSet)
2. Write dialect-agnostic Flyway migrations
3. Add PersistentVolumeClaim for PostgreSQL data
4. Separate dev/prod profiles with proper datasource config

### Phase 3 — API & Domain

1. Response DTOs for all endpoints — never expose entities
2. Link `News.author` to `ApiUser` via FK → ownership validation
3. Add user registration (`POST /users`)
4. Add tag management endpoints
5. Standardize pagination envelope

### Phase 4 — Frontend Modernization

Incremental upgrade: Angular 10 → 11 → 12 → ... → 17+

Key milestones:
- v12: Replace tslint with ESLint (`@angular-eslint`)
- v14: Standalone components available
- v16: Angular Signals preview
- v17: New `@if`/`@for` control flow, esbuild default

Also: Node 14 → 20 LTS in Docker; update Angular Material alongside core version.

### Phase 5 — Observability

1. Spring Boot Actuator (health, metrics endpoints)
2. Prometheus + Grafana (k3s add-ons)
3. Structured logging (Logback → JSON format)
4. Distributed tracing (Micrometer + Zipkin)

---

## 7. DevOps & Infra

### 7.1 CI/CD Pipeline (Jenkins)

```
Git Push → Checkout → Build API (Gradle) → Test API (JUnit)
  → Build Client (npm ci + ng build) → Docker Build & Push (API + Client)
  → Deploy to k3s (kubectl set image) → Verify (rollout status)
```

- Images tagged with `${BUILD_NUMBER}` and `latest`
- Registry: `192.168.0.106:5000`
- Secrets via Jenkins credentials (never inline)
- `API_BASE_URL=""` for client build (relative paths + Nginx proxy)
- Jenkinsfile lives at repo root; use declarative pipeline format

### 7.2 Versioning

- Semantic versioning: `MAJOR.MINOR.PATCH`
- Git tags trigger versioned builds; Docker tags: `v1.2.0` + `latest`
- Gradle version from env: `System.getenv("APP_VERSION") ?: '0.0.1-SNAPSHOT'`

### 7.3 Environment Separation

| Environment | Database     | Registry            | Profile |
|-------------|--------------|---------------------|---------|
| Local       | H2 in-memory | local Docker        | default |
| Homelab     | PostgreSQL   | 192.168.0.106:5000  | prod    |

### 7.4 Target State Diagram

```
         Developer
            │
       git push
            │
            ▼
        Jenkins (192.168.0.106)
       ┌────┴────┐
       │         │
  Build+Test  Docker Build+Push
       │         │
       └────┬────┘
            ▼
    Private Registry (192.168.0.106:5000)
            │
            ▼
      k3s Cluster
  ┌──────────────────────┐
  │  Namespace: myblog   │
  │                      │
  │  Ingress (Traefik)   │ ← myblog.homelab.local
  │    │          │      │
  │    ▼          ▼      │
  │  client    api-svc   │
  │  (nginx)   (Spring)  │
  │              │       │
  │           postgres   │
  │           + PVC      │
  └──────────────────────┘
```
