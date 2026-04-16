> ⚠️ SOURCE OF TRUTH — Edit here. Sync proxy after changes.
> Proxy: `.github/docs/ai-context.md`

# AI Context — MyBlog

> Domain model, business rules, API surface, and tech stack.
> For system structure and deployment: see `architecture.md`.
> For coding rules: see `.github/copilot-instructions.md`.

---

## System Purpose

**MyBlog** is a fullstack news/blog publishing platform. Registered users publish, edit, and
delete news articles. Anonymous visitors browse, search by title or tag, and post comments.
Authentication is handled via JWT.

Originally a technical evaluation exercise, now evolving into a production-ready system
deployed on a homelab (k3s, Docker, Jenkins CI/CD).

---

## Technology Stack

| Layer       | Technology                                |
|-------------|-------------------------------------------|
| Backend     | Java 21, Spring Boot 3.4.3                |
| Security    | Spring Security 6, JWT (jjwt 0.12.3)      |
| Persistence | Spring Data JPA, Hibernate, Flyway, H2    |
| Build       | Gradle (Groovy DSL)                       |
| Frontend    | Angular 10.1.6, Angular Material 10.2.7   |
| Server      | Nginx (serving Angular SPA)               |
| Runtime     | Docker, docker-compose                    |
| Target Env  | k3s (Kubernetes), Private Docker Registry |
| CI/CD       | Jenkins                                   |

---

## Domain Model

### News

The primary content entity. Represents a published article.

| Field      | Type            | Notes                                       |
|------------|-----------------|---------------------------------------------|
| `id`       | Long            | PK, auto-generated                          |
| `title`    | String          |                                             |
| `author`   | String          | Free-text — **not linked to ApiUser** (gap) |
| `date`     | LocalDateTime   |                                             |
| `content`  | String (LOB)    |                                             |
| `tags`     | List\<String\>  | Join table `news_tags`, free-form           |
| `comments` | List\<Comment\> | One-to-many, eagerly loaded                 |

- Default ordering: `id DESC` (newest first)
- Supports pagination and search by title or tag

### Comment

Belongs to a News article (many-to-one).

| Field     | Type          | Notes                                 |
|-----------|---------------|---------------------------------------|
| `id`      | Long          | PK                                    |
| `comment` | String (LOB)  |                                       |
| `date`    | LocalDateTime |                                       |
| `author`  | String        | Free-text — **not linked to ApiUser** |
| `news`    | News          | FK to parent News                     |

- No edit/delete endpoints yet

### ApiUser

Authenticated system user. Implements Spring Security `UserDetails`.

| Field      | Type            | Notes                         |
|------------|-----------------|-------------------------------|
| `id`       | Long            | PK                            |
| `email`    | String (unique) | Used as username              |
| `password` | String          | BCrypt-hashed                 |
| `roles`    | Set\<Role\>     | Many-to-many via `user_roles` |
| `created`  | LocalDateTime   |                               |
| `active`   | boolean         | Soft-disable without deletion |

- Seed users inserted via Flyway migration (`V1__initial_data.sql`)
- No self-registration endpoint yet

### Role

- Values: `ROLE_ADMIN`, `ROLE_USER`
- Many-to-many with ApiUser

### RefreshToken

- Persisted in DB for JWT refresh lifecycle
- Invalidated on logout; expires after 7 days

### Tag

- Not a formal entity — `List<String>` in `news_tags` collection table
- No CRUD endpoints; filtering via `NewsRepository.findByTags()`

---

## Authentication Flow (JWT)

1. `POST /auth/login` with `{ email, password }`
2. Server validates via `CustomUserDetailsService` → returns `{ accessToken, refreshToken }`
3. Access token: **15 min** (HS256). Refresh token: **7 days** (persisted in DB)
4. Client stores tokens in `sessionStorage` (not localStorage — reduced XSS window)
5. Client auto-schedules refresh 5 min before expiry
6. `POST /auth/refresh` with `{ refreshToken }` → new token pair
7. `POST /auth/logout` → deletes refresh token from DB
8. `JwtInterceptor` attaches `Authorization: Bearer <token>` to all requests

---

## API Endpoints

| Method | Path                  | Auth        | Description                      |
|--------|-----------------------|-------------|----------------------------------|
| POST   | /auth/login           | No          | Authenticate, get JWT tokens     |
| POST   | /auth/refresh         | No          | Refresh access token             |
| POST   | /auth/logout          | Yes         | Invalidate refresh token         |
| GET    | /news                 | No          | List/search news (paginated)     |
| GET    | /news/{id}            | No          | Full news article with comments  |
| GET    | /news/topic?tag={tag} | No          | Filter news by tag               |
| POST   | /news                 | ⚠️ No (bug) | Create news article              |
| PUT    | /news/{id}            | ⚠️ No (bug) | Update news article              |
| DELETE | /news/{id}            | ⚠️ No (bug) | Delete news article              |
| POST   | /news/{id}            | No          | Add comment to news article      |

**Pagination**: `page` (0-based), `size`, `sort` (default: `id,desc`)

> ⚠️ `POST/PUT/DELETE /news` should require auth but don't due to SecurityConfig bugs.
> See `architecture.md` §5 for the full security issues list.

---

## Business Rules

1. Any visitor can read news and comments (no auth required)
2. Only authenticated users should create/edit/delete news (partially enforced — see architecture.md §5)
3. Anyone can post comments (including anonymous)
4. News author should be able to delete own posts (not yet implemented)
5. Posts ordered chronologically, newest first
6. Tags are free-form strings, unmoderated
7. User accounts can be deactivated (`active=false`) without deletion
8. Passwords hashed with BCrypt (default strength 10)
9. Refresh tokens server-side persisted; full rotation not yet implemented

---

## Frontend Communication

- Angular services (`NewsService`, `AuthService`) make all HTTP calls — never directly from components
- Base API URL from `environment.ts` / `environment.prod.ts`
- `JwtInterceptor` attaches Authorization header automatically
- Frontend interfaces: `NewsPageable`, `FullNews`, `Post`, `CommentResponse`
- In Docker: `API_BASE_URL=http://127.0.0.1:8080` — works locally but breaks in Kubernetes
  (see `architecture.md` §5.4 for details)
