> ⚠️ SOURCE OF TRUTH — Edit here. Sync proxy after changes.
> Proxy: `.github/docs/architecture.md`

# Architecture — MyBlog

> Living architecture document. Keep this updated as the system evolves.

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
myblog/                          ← project root
├── api/                         ← Spring Boot backend
│   ├── src/main/java/br/com/mekylei/myblog/
│   │   ├── auth/                ← JWT filter + utility
│   │   ├── configurations/      ← SecurityConfig, BeanConfig, WebConfig
│   │   ├── controllers/         ← REST controllers (News, Comment, Auth)
│   │   ├── dtos/                ← Request/Response DTOs
│   │   ├── enums/               ← NotFoundBy, etc.
│   │   ├── exceptions/          ← ApiExceptionHandler + custom exceptions
│   │   ├── models/              ← JPA entities (News, Comment, ApiUser, Role, RefreshToken)
│   │   ├── repositories/        ← Spring Data JPA interfaces
│   │   ├── services/            ← Business logic
│   │   └── utils/               ← DateUtil
│   ├── src/main/resources/
│   │   ├── application.yml      ← App config (H2, Flyway, JWT secret ref)
│   │   └── db/specific/h2/      ← Flyway migration scripts
│   └── Dockerfile               ← Multi-stage build (JDK → JRE)
│
├── client/                      ← Angular frontend
│   ├── src/app/
│   │   ├── auth/                ← AuthService, JwtInterceptor
│   │   ├── core/                ← Header, Footer components
│   │   ├── home/                ← Home module
│   │   ├── news/                ← News module (lazy loaded)
│   │   │   ├── news-list/       ← List view
│   │   │   ├── news-details/    ← Detail view + comments
│   │   │   └── news-search/     ← Search component
│   │   └── shared/              ← Interfaces, services, components, utils
│   ├── src/environments/        ← environment.ts / environment.prod.ts
│   ├── config/nginx-custom.conf ← Nginx SPA routing config
│   └── Dockerfile               ← Multi-stage (Node 14 → nginx:alpine)
│
├── docker-compose.yml           ← Orchestration for Linux
├── docker-compose-windows.yml   ← Orchestration for Windows
└── README.md
```

### 1.2 Backend Layered Architecture

```
┌──────────────────────────────────────┐
│             HTTP Request             │
└──────────────┬───────────────────────┘
               │
┌──────────────▼───────────────────────┐
│  JwtFilter (OncePerRequestFilter)    │  ← validates Bearer token
└──────────────┬───────────────────────┘
               │
┌──────────────▼───────────────────────┐
│  SecurityFilterChain                 │  ← authorization rules
└──────────────┬───────────────────────┘
               │
┌──────────────▼───────────────────────┐
│  Controller Layer                    │
│  NewsController / CommentController  │  ← input validation (@Valid)
│  AuthController                      │
└──────────────┬───────────────────────┘
               │  DTOs
┌──────────────▼───────────────────────┐
│  Service Layer                       │
│  NewsService / CommentService        │  ← business logic
│  AuthService                         │
└──────────────┬───────────────────────┘
               │  Entities
┌──────────────▼───────────────────────┐
│  Repository Layer (Spring Data JPA)  │
│  NewsRepository / CommentRepository  │  ← database queries
│  UserRepository / RoleRepository     │
│  RefreshTokenRepository              │
└──────────────┬───────────────────────┘
               │
┌──────────────▼───────────────────────┐
│  H2 In-Memory Database               │  ← schema managed by Flyway
└──────────────────────────────────────┘
```

#### ⚠️ Current Architecture Violations

The diagram above shows the *intended* flow. The following violations exist in the actual code:

| Violation | Location | Impact |
|-----------|----------|--------|
| `NewsController` returns `News` entity from create/update/delete/list/topic endpoints | NewsController.java | Exposes JPA internals directly to API consumers |
| `NewsService` injects `PagedResourcesAssembler` — a Spring HATEOAS (web/presentation) class | NewsService.java | Couples service layer to the presentation framework |
| `News.toFullComment()` performs DTO mapping inside the JPA entity | News.java | Violates SRP; mapping logic on a persistence object |
| `@JsonBackReference` on `News.comment`; `@JsonManagedReference` on `Comment.news` | News.java, Comment.java | Entities control their own serialization — should be DTO's job |
| `AuthResponse` DTO imports `JwtUtil.ACCESS_TOKEN_EXPIRES_IN` at compile time | AuthResponse.java | DTO package depends on auth infrastructure class |

### 1.3 Frontend Angular Module Structure

```
AppModule
├── AppRoutingModule        ← root routing
├── HomeModule              ← /home route
├── NewsModule (lazy)       ← /postagem route
│   ├── NewsComponent       ← container
│   ├── NewsListComponent   ← paginated post list
│   ├── NewsDetailsComponent← full post + comments
│   └── NewsSearchComponent ← search bar
├── SharedModule
│   ├── components/         ← AuthDialog, PageNotFound, etc.
│   ├── interfaces/         ← TypeScript interfaces matching API shapes
│   ├── services/           ← shared injectable services
│   └── utils/              ← DebugUtil, etc.
└── CoreModule
    ├── HeaderModule        ← top navigation bar + auth controls
    └── FooterModule        ← footer
```

**Key services:**
- `NewsService` — all news CRUD and search HTTP calls
- `AuthService` — login, logout, token refresh, auth state (BehaviorSubject)
- `JwtInterceptor` — attaches `Authorization: Bearer <token>` to outgoing requests

---

## 2. Runtime Architecture

### 2.1 Current (docker-compose)

```
┌──────────────────────────────────────────────────────┐
│                  Docker Host (Linux)                  │
│                                                       │
│  ┌──────────────────────┐  ┌──────────────────────┐  │
│  │  client-myblog       │  │  api-myblog          │  │
│  │  nginx:alpine        │  │  eclipse-temurin:21  │  │
│  │  port 8082:80        │  │  port 8080:8080      │  │
│  │                      │  │                      │  │
│  │  Angular SPA         │  │  Spring Boot API     │  │
│  │  (static files)      │  │  + H2 in-memory DB   │  │
│  └──────────┬───────────┘  └──────────────────────┘  │
│             │ serves                ▲                 │
└─────────────┼──────────────────────┼─────────────────┘
              │                      │
           Browser ─── XHR ─────────┘
          (port 8082)          (port 8080)
```

**Notes:**
- Both services share the Docker default bridge network; containers can reach each other by container name.
- The Angular app is built with `API_BASE_URL=http://127.0.0.1:8080` — this resolves on the **browser's** side,
  meaning a user's browser must be able to reach `127.0.0.1:8080`. This works on a local machine but is incorrect
  for remote deployments.
- No DB container — H2 is embedded and lives inside the JVM process.
- Data is **not persisted** between restarts.

### 2.2 Ports

| Service       | Container Port | Host Port | Protocol |
|---------------|---------------|-----------|----------|
| api-myblog    | 8080          | 8080      | HTTP     |
| client-myblog | 80            | 8082      | HTTP     |
| H2 console    | 8080          | 8080      | HTTP     |

---

## 3. Deployment Architecture

### 3.1 Homelab Context

```
Homelab Server: 192.168.0.106
├── Docker (standalone + compose)
├── Private Docker Registry (e.g., 192.168.0.106:5000)
├── k3s (Kubernetes)
│   └── kubectl context available on server
└── Jenkins (CI/CD)
    └── Pipelines triggered by Git push or manually
```

### 3.2 Docker Image Strategy

Images should be tagged with both a version and `latest`:

```
192.168.0.106:5000/myblog-api:1.0.0
192.168.0.106:5000/myblog-api:latest
192.168.0.106:5000/myblog-client:1.0.0
192.168.0.106:5000/myblog-client:latest
```

Build args and environment variables:

| Variable             | Where              | How to inject           |
|----------------------|--------------------|-------------------------|
| `JWT_SECRET`         | api container      | Kubernetes Secret       |
| `SPRING_DATASOURCE_URL` | api container   | Kubernetes ConfigMap    |
| `API_BASE_URL`       | client build arg   | Build-time ARG          |

### 3.3 Target Kubernetes Architecture (k3s)

```
k3s Cluster (192.168.0.106)
│
├── Namespace: myblog
│   │
│   ├── Deployment: myblog-api          (2 replicas)
│   │   └── Pod: myblog-api
│   │       ├── image: 192.168.0.106:5000/myblog-api:1.0.0
│   │       ├── port: 8080
│   │       └── envFrom: Secret/myblog-api-secrets
│   │
│   ├── Service: myblog-api-svc         (ClusterIP :8080)
│   │
│   ├── Deployment: myblog-client       (2 replicas)
│   │   └── Pod: myblog-client
│   │       ├── image: 192.168.0.106:5000/myblog-client:1.0.0
│   │       └── port: 80
│   │
│   ├── Service: myblog-client-svc      (ClusterIP :80)
│   │
│   └── Ingress: myblog-ingress
│       ├── /        → myblog-client-svc:80
│       └── /api/    → myblog-api-svc:8080   (strip prefix)
│
└── PersistentVolumeClaim (future — for PostgreSQL)
```

### 3.4 Kubernetes Manifest Examples

#### Namespace
```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: myblog
```

#### Secret (JWT)
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: myblog-api-secrets
  namespace: myblog
type: Opaque
stringData:
  JWT_SECRET: "replace-with-real-secret-in-vault"
```

#### Backend Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: myblog-api
  namespace: myblog
spec:
  replicas: 2
  selector:
    matchLabels:
      app: myblog-api
  template:
    metadata:
      labels:
        app: myblog-api
    spec:
      containers:
        - name: myblog-api
          image: 192.168.0.106:5000/myblog-api:1.0.0
          ports:
            - containerPort: 8080
          envFrom:
            - secretRef:
                name: myblog-api-secrets
          readinessProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            initialDelaySeconds: 15
            periodSeconds: 10
          livenessProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 30
          resources:
            requests:
              memory: "256Mi"
              cpu: "200m"
            limits:
              memory: "512Mi"
              cpu: "500m"
```

#### Backend Service
```yaml
apiVersion: v1
kind: Service
metadata:
  name: myblog-api-svc
  namespace: myblog
spec:
  selector:
    app: myblog-api
  ports:
    - port: 8080
      targetPort: 8080
```

#### Frontend Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: myblog-client
  namespace: myblog
spec:
  replicas: 2
  selector:
    matchLabels:
      app: myblog-client
  template:
    metadata:
      labels:
        app: myblog-client
    spec:
      containers:
        - name: myblog-client
          image: 192.168.0.106:5000/myblog-client:1.0.0
          ports:
            - containerPort: 80
          readinessProbe:
            httpGet:
              path: /
              port: 80
            initialDelaySeconds: 5
            periodSeconds: 10
          livenessProbe:
            httpGet:
              path: /
              port: 80
            initialDelaySeconds: 10
            periodSeconds: 30
          resources:
            requests:
              memory: "64Mi"
              cpu: "50m"
            limits:
              memory: "128Mi"
              cpu: "200m"
```

#### Ingress (Traefik — default in k3s)
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: myblog-ingress
  namespace: myblog
  annotations:
    traefik.ingress.kubernetes.io/router.entrypoints: web
spec:
  rules:
    - host: myblog.homelab.local
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: myblog-client-svc
                port:
                  number: 80
          - path: /api
            pathType: Prefix
            backend:
              service:
                name: myblog-api-svc
                port:
                  number: 8080
```

> **Note**: When using the `/api` prefix in Kubernetes, the Spring Boot `@RequestMapping` paths must
> either be prefixed with `/api/` in code, or a Traefik StripPrefix middleware must be applied.

### 3.5 Environment Configuration Strategy

Use Spring Boot profiles to separate configurations:

| Profile  | Database        | H2 Console | JWT Secret   | Log Level |
|----------|-----------------|------------|--------------|-----------|
| `default`| H2 in-memory    | Enabled    | Env var      | DEBUG     |
| `prod`   | PostgreSQL      | Disabled   | K8s Secret   | INFO      |

Files:
```
api/src/main/resources/
├── application.yml          ← shared defaults
├── application-dev.yml      ← local dev overrides
└── application-prod.yml     ← production config (PostgreSQL, no H2 console)
```

---

## 4. Data Flow

### 4.1 Request Lifecycle (Public Read)

```
Browser
  │
  │  GET /news?page=0&size=5       (port 8082 → Nginx)
  ▼
Nginx (client-myblog)
  │  serves index.html → Angular app bootstraps
  │
Angular App
  │  NewsService.getPagedNews(0, 5)
  │  GET http://[host]:8080/news?page=0&size=5
  ▼
Spring Boot (api-myblog :8080)
  │
  ├── JwtFilter: no Authorization header → skip authentication
  ├── SecurityFilterChain: GET /news → permitAll
  ├── NewsController.getNews(pageable, title=null)
  ├── NewsService.getNews(pageable)       ← applies default sort: id DESC
  ├── NewsRepository.findAll(pageable)    ← JPA query to H2
  │
  │  Returns: Page<News> → pagedResourcesAssembler.toModel()
  ▼
JSON Response (HAL+JSON paged)
  │
Angular App
  │  maps to NewsPageable interface
  ▼
NewsListComponent renders posts
```

### 4.2 Authentication Flow (JWT Lifecycle)

```
Browser
  │  POST /auth/login { email, password }
  ▼
AuthController.login()
  │
  ├── AuthService.login(email, password)
  ├── UserDetailsService.loadUserByUsername(email)
  ├── PasswordEncoder.matches(raw, hashed)  ← BCrypt
  ├── JwtUtil.generateAccessToken(email)    ← HS256, 15min
  ├── JwtUtil.generateRefreshToken(email)   ← HS256, 7days
  └── RefreshTokenRepository.save(token)    ← persists refresh token
  │
  │  Returns: { accessToken, refreshToken }
  ▼
Angular AuthService
  │  Stores tokens in sessionStorage
  │  Schedules refresh at (expiry - 5 minutes)
  │
  │  Subsequent requests:
  ▼
JwtInterceptor
  │  Adds: Authorization: Bearer <accessToken>
  ▼
JwtFilter (backend)
  │  Validates token signature + expiry
  │  Extracts email → loads UserDetails
  │  Sets SecurityContext → request proceeds
  ▼
Protected Endpoint

  ── Token Refresh ──
Browser (auto, 5min before expiry)
  │  POST /auth/refresh { refreshToken }
  ▼
AuthController.refresh()
  │  Validates refreshToken from DB
  │  Issues new accessToken + refreshToken
  │  Deletes old refreshToken (rotation)
  ▼
Angular: updates sessionStorage, reschedules refresh
```

---

## 5. Known Problems

### 5.1 Data & Persistence
- **H2 in-memory**: All data is erased on every container restart. No production use case.
- **No data migration strategy**: When switching to PostgreSQL, Flyway scripts are H2-specific (SQL dialect differences).

### 5.2 Security
- `JWT_SECRET` baked into `Dockerfile` as an `ENV` instruction — visible in image layers.
- H2 console enabled with `web-allow-others: true` — any network client can browse the DB.
- **`SecurityConfig` matchers for `PUT`/`DELETE` use `/news` not `/news/**`** — `PUT /news/1` and `DELETE /news/1` fall through to `anyRequest().permitAll()` and are completely unprotected.
- `POST /news` (create post) is not protected even though business rules require authentication.
- No rate limiting on authentication endpoints — brute-force attack surface.
- No HTTPS — traffic is plain HTTP, including credentials.
- **CORS: `allowedOriginPatterns("*")` with `allowCredentials(true)`** in `WebConfig` — any origin can make credentialed cross-site requests. Must be locked down in production.
- **`JwtUtil.validateToken()` swallows all exceptions** with `catch (Exception e) { return null; }` — authentication failures are silent and unlogged.

### 5.3 API Design
- `NewsController` returns `News` entity directly in POST/PUT/DELETE responses — exposes JPA internals.
- `News.author` is a free-text field — no ownership validation; anyone with auth can overwrite any post's author.
- No API versioning strategy (`/v1/...`).
- No standard response envelope — mixed use of raw entity, DTO, and HAL responses.

### 5.4 Frontend
- Angular 10 is 4+ major versions behind; multiple known CVEs in transitive dependencies.
- Node 14 in Docker is EOL since 2023.
- `API_BASE_URL=http://127.0.0.1:8080` only works when browser and API are on the same machine.
- Tokens stored in `sessionStorage` — lost on tab close; no "remember me" mechanism.

### 5.5 Tight Coupling
- `News` entity is serialized with `@JsonBackReference` / `@JsonManagedReference` — business serialization logic embedded in JPA entity.
- `ApiUser` implements `UserDetails` directly — mixing security interface with domain model.
- Frontend interfaces directly mirror backend entity shapes, not API contracts.
- `NewsService` depends on `PagedResourcesAssembler<News>` — a Spring HATEOAS web-presentation class injected into the service/business layer.
- `AuthResponse` DTO references `JwtUtil.ACCESS_TOKEN_EXPIRES_IN` — the DTO package has a compile-time dependency on the auth infrastructure class; values should flow through configuration.
- `Post.id` typed as `string` in the Angular interface but the backend returns `number` (Java `Long`) — silent type coercion bug with no compile-time error.

### 5.6 Scalability
- H2 in-memory cannot be shared across multiple pod replicas.
- No caching layer.
- No async processing for notifications or heavy reads.

---

## 6. Improvement Roadmap

### Phase 1 — Security & Stability (Immediate)

1. **Extract `JWT_SECRET`** from Dockerfile → inject via environment variable / Kubernetes Secret.
2. **Disable H2 console** in any non-local profile.
3. **Protect `POST /news`** with authentication in `SecurityConfig`.
4. **Add `application-prod.yml`** with H2 console disabled and a placeholder for PostgreSQL.
5. **Fix `API_BASE_URL`** — use relative paths or Nginx reverse proxy instead of absolute `127.0.0.1`.

### Phase 2 — Data Layer Migration

1. Replace H2 with **PostgreSQL** (containerized in docker-compose and as a k8s StatefulSet).
2. Write **dialect-agnostic** Flyway migrations (avoid H2-specific SQL).
3. Add `PersistentVolumeClaim` to k3s for PostgreSQL data.
4. Introduce `application-dev.yml` for H2 local dev and `application-prod.yml` for PostgreSQL.

### Phase 3 — API & Domain Improvement

1. Introduce **response DTOs** for all endpoints — never expose entities directly.
2. **Link `News.author` to `ApiUser`** via FK — ownership validation before update/delete.
3. **Link `Comment.author` to `ApiUser`** (optional for anonymous guest comments).
4. Add **user registration** endpoint (`POST /users`).
5. Add **tag management** endpoints.
6. Add **API versioning** under `/v1/...`.
7. Standardize pagination envelope.

### Phase 4 — Frontend Modernization

Incremental Angular upgrade path:
```
Angular 10 → 11 → 12 → 13 → 14 → 15 → 16 → 17 (current)
```
Key changes at each major version:
- v12: Ivy compilation (likely already used), remove ViewEngine support
- v13: ESBuild support, `NgModules` optional paths
- v14: Standalone components available
- v15: `inject()` function, standalone APIs
- v16: Signals preview
- v17: `@if`, `@for` new control flow, esbuild default

During upgrade:
- Update Node to 20 LTS in Docker
- Update Angular Material alongside Angular core
- Replace `tslint.json` with ESLint
- Migrate to standalone components gradually

### Phase 5 — Observability

1. Add Spring Boot Actuator with health, info, metrics endpoints.
2. Integrate with Prometheus + Grafana (available as k3s add-ons).
3. Add structured logging (Logback → JSON format for log aggregation).
4. Introduce distributed tracing (Micrometer + Zipkin).

---

## 7. DevOps & Infra

### 7.1 CI/CD Pipeline (Jenkins)

#### Pipeline Overview

```
Git Push (main/develop)
       │
       ▼
┌──────────────┐
│  Checkout    │  git clone, set version from git tag or build number
└──────┬───────┘
       │
       ▼
┌──────────────┐
│  Build API   │  ./gradlew clean bootJar (inside agent with JDK21)
└──────┬───────┘
       │
       ▼
┌──────────────┐
│  Test API    │  ./gradlew test (JUnit5 + Spring Boot Test)
└──────┬───────┘
       │
       ▼
┌──────────────┐
│  Build Client│  npm ci && npm run build -- --configuration=production
└──────┬───────┘
       │
       ▼
┌──────────────────────────────┐
│  Docker Build & Push (API)   │
│  docker build -t 192.168.0.106:5000/myblog-api:$VERSION .
│  docker push 192.168.0.106:5000/myblog-api:$VERSION
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│  Docker Build & Push (Client)│
│  docker build --build-arg API_BASE_URL=http://myblog-api-svc:8080 \
│    -t 192.168.0.106:5000/myblog-client:$VERSION .
│  docker push 192.168.0.106:5000/myblog-client:$VERSION
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│  Deploy to k3s               │
│  kubectl set image deployment/myblog-api \
│    myblog-api=192.168.0.106:5000/myblog-api:$VERSION -n myblog
│  kubectl set image deployment/myblog-client \
│    myblog-client=192.168.0.106:5000/myblog-client:$VERSION -n myblog
│  kubectl rollout status deployment/myblog-api -n myblog
└──────────────────────────────┘
```

#### Jenkinsfile (Declarative Pipeline Skeleton)

```groovy
pipeline {
    agent any

    environment {
        REGISTRY = '192.168.0.106:5000'
        API_IMAGE = "${REGISTRY}/myblog-api"
        CLIENT_IMAGE = "${REGISTRY}/myblog-client"
        VERSION = "${env.BUILD_NUMBER}"
        JWT_SECRET = credentials('myblog-jwt-secret')  // Jenkins credential
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build API') {
            steps {
                dir('api') {
                    sh 'chmod +x gradlew'
                    sh './gradlew clean bootJar -x test'
                }
            }
        }

        stage('Test API') {
            steps {
                dir('api') {
                    sh './gradlew test'
                }
            }
            post {
                always {
                    junit 'api/build/test-results/**/*.xml'
                }
            }
        }

        stage('Build Client') {
            steps {
                dir('client') {
                    sh 'npm ci'
                    sh 'npm run build -- --configuration=production'
                }
            }
        }

        stage('Docker Build & Push') {
            steps {
                sh """
                    docker build -t ${API_IMAGE}:${VERSION} -t ${API_IMAGE}:latest ./api
                    docker push ${API_IMAGE}:${VERSION}
                    docker push ${API_IMAGE}:latest

                    # API_BASE_URL is empty: Angular uses relative paths (e.g., /api/news)
                    # Nginx in the client container proxies /api/ to myblog-api-svc:8080
                    # NEVER use http://myblog-api-svc:8080 here — that resolves inside the cluster,
                    # not in the user's browser
                    docker build \\
                        --build-arg API_BASE_URL="" \\
                        -t ${CLIENT_IMAGE}:${VERSION} \\
                        -t ${CLIENT_IMAGE}:latest \\
                        ./client
                    docker push ${CLIENT_IMAGE}:${VERSION}
                    docker push ${CLIENT_IMAGE}:latest
                """
            }
        }

        stage('Deploy to k3s') {
            steps {
                sh """
                    kubectl set image deployment/myblog-api \\
                        myblog-api=${API_IMAGE}:${VERSION} -n myblog
                    kubectl set image deployment/myblog-client \\
                        myblog-client=${CLIENT_IMAGE}:${VERSION} -n myblog
                    kubectl rollout status deployment/myblog-api -n myblog --timeout=120s
                    kubectl rollout status deployment/myblog-client -n myblog --timeout=120s
                """
            }
        }
    }

    post {
        failure {
            echo 'Pipeline failed. Check logs and notify team.'
        }
        success {
            echo "Deployed version ${VERSION} successfully."
        }
    }
}
```

### 7.2 Versioning Strategy

- Use **semantic versioning** (SemVer): `MAJOR.MINOR.PATCH`
- Git tags trigger versioned builds: `git tag v1.2.0 && git push --tags`
- Docker image tags: `v1.2.0` and `latest`
- Gradle version synced with git tag via CI build:
  ```groovy
  version = System.getenv("APP_VERSION") ?: '0.0.1-SNAPSHOT'
  ```

### 7.3 Environment Separation

| Environment | Database       | Registry              | Profile  | How                        |
|-------------|----------------|-----------------------|----------|----------------------------|
| Local       | H2 in-memory   | local Docker          | default  | `docker-compose up`        |
| Homelab     | PostgreSQL pod | 192.168.0.106:5000    | prod     | k3s manifests + Secrets    |
| Prod-like   | PostgreSQL     | Private cloud registry| prod     | Helm chart (future)        |

### 7.4 System Architecture Diagram (Target State)

```
                Developer Workstation
                        │
               git push to repo
                        │
                        ▼
                   Jenkins
                  (192.168.0.106)
           ┌────────────┴────────────┐
           │                         │
    Build + Test             Build Docker Images
    (Gradle + npm)           & Push to Registry
           │                         │
           └──────────┬──────────────┘
                      │
                      ▼
            Private Registry
          (192.168.0.106:5000)
                      │
                      ▼
               k3s Cluster
          ┌────────────────────────┐
          │  Namespace: myblog     │
          │                        │
          │  ┌──────────────────┐  │
          │  │  Ingress         │  │◄── http://myblog.homelab.local
          │  └───────┬──────────┘  │
          │          │             │
          │  ┌───────┴──────────┐  │
          │  │  client-svc :80  │  │
          │  └───────┬──────────┘  │
          │          │             │
          │   ┌──────▼────────┐    │
          │   │ client pods   │    │
          │   │ (nginx+SPA)   │    │
          │   └───────────────┘    │
          │                        │
          │  ┌───────────────────┐ │
          │  │  api-svc :8080    │ │
          │  └────────┬──────────┘ │
          │           │            │
          │   ┌───────▼──────────┐ │
          │   │ api pods x2      │ │
          │   │ (Spring Boot)    │ │
          │   └──────────────────┘ │
          │                        │
          │  ┌───────────────────┐ │
          │  │  postgres-svc     │ │
          │  └────────┬──────────┘ │
          │           │            │
          │   ┌───────▼──────────┐ │
          │   │ postgres pod     │ │
          │   │ + PVC (data)     │ │
          │   └──────────────────┘ │
          └────────────────────────┘
```
