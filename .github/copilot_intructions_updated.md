# GitHub Copilot Instructions — TurtleShop

> This file lives at `.github/copilot-instructions.md` and is automatically
> picked up by GitHub Copilot in VS Code and JetBrains IDEs.
> Its purpose is to keep Copilot grounded in the actual architecture of this
> project and prevent hallucinated patterns that don't match the codebase.

---

## Project Overview

**TurtleShop** is a full-stack e-commerce application consisting of:

- `backend/turtleshop-api` — Spring Boot 3.4.5, Java 25, Gradle (Kotlin DSL)
- `frontend/turtleshop-web` — React 19, TypeScript, Vite, React Router v7
- Infrastructure: Docker Compose, PostgreSQL 16, MongoDB, Redis (planned)

The project is in active mid-development. Core features (auth, products,
categories, cart, wishlist, orders, checkout, inventory, shipment, reviews,
transactions) are implemented. Planned additions are listed at the bottom of
this file.

---

## Backend Architecture

### Package Structure

All backend code lives under `org.turtleshop.api`. Every domain is a
self-contained module under `modules/`:

```
org.turtleshop.api
├── config/          # Spring Security, JWT filter, OpenAPI, AdminInitializer
├── modules/
│   ├── auth/        # Customer + SystemRole entities, JWT auth
│   ├── product/     # Product entity (PostgreSQL)
│   ├── category/    # Product categories (PostgreSQL)
│   ├── productcategory/  # Many-to-many join (PostgreSQL)
│   ├── cart/        # Cart + CartItem (PostgreSQL)
│   ├── wishlist/    # Wishlist + WishlistItem (PostgreSQL)
│   ├── order/       # Order + OrderItem (PostgreSQL)
│   ├── checkout/    # Checkout orchestration service (PostgreSQL)
│   ├── inventory/   # Inventory management (PostgreSQL)
│   ├── shipment/    # Shipment + ShipmentStatusLog (PostgreSQL)
│   ├── transaction/ # Transaction + PaymentMethod (PostgreSQL)
│   └── reviews/     # Reviews (MongoDB only)
```

Each module follows the same internal layout:
`controller/` → `service/` → `repository/` (named `*Access`) → `model/` + `dto/` + `enums/`

### Database Split — Critical

**PostgreSQL** (via `NamedParameterJdbcTemplate`) handles all relational data.
**MongoDB** (via `MongoRepository`) handles reviews only.

Do NOT suggest JPA/Hibernate, `@Entity`, `EntityManager`, or Spring Data JPA
for any module. The project deliberately uses raw JDBC with named parameters.

**PostgreSQL repositories** follow this pattern exactly:
```java
@Repository
@RequiredArgsConstructor
public class SomethingAccess {
    private final NamedParameterJdbcTemplate jdbc;

    private final RowMapper<SomethingModel> mapper = (rs, rowNum) -> { ... };

    public List<SomethingModel> findAll() {
        return jdbc.query("SELECT ... FROM SOMETHING", mapper);
    }

    public Optional<SomethingModel> findById(int id) {
        return jdbc.query(sql, new MapSqlParameterSource("id", id), mapper)
                   .stream().findFirst();
    }
    // insert / update / delete follow same pattern
}
```

**MongoDB repositories** extend `MongoRepository<Model, String>` and are
interfaces only (no implementation class):
```java
public interface ReviewAccess extends MongoRepository<ReviewModel, String> {
    List<ReviewModel> findByProductId(Integer productId);
}
```

Schema changes to PostgreSQL tables go in a new Flyway migration file under
`src/main/resources/db/migration/` following the existing `V{n}__description.sql`
naming convention. Never suggest altering tables inline in Java code.

### Security & JWT — Critical

The auth system is fully custom. Do NOT suggest Spring Security's
`UserDetailsService`, `AuthenticationManager`, `AuthenticationProvider`,
or form-based login. None of these are wired up.

**How it actually works:**
- `JwtAuthenticationFilter` (extends `OncePerRequestFilter`) intercepts every
  request, extracts the Bearer token, calls `JwtService`, and sets a
  `UsernamePasswordAuthenticationToken` in the `SecurityContextHolder`.
- `JwtService` uses `io.jsonwebtoken` (JJWT 0.12.6) with HMAC-SHA key derived
  from `${app.auth.jwt-secret}` (min 32 chars). Token subject = customer email.
  Roles are stored as a `List<String>` claim named `"roles"`.
- `SecurityConfig` is stateless (no session), CSRF disabled. Public endpoints:
  `/api/auth/**`, `/health`, `/error`, Swagger `/v3/api-docs/**`, `/swagger-ui/**`.
  Everything else requires a valid JWT.
- Authorization on endpoints uses `@PreAuthorize` (method security is enabled
  via `@EnableMethodSecurity`).
- Passwords are hashed with `BCryptPasswordEncoder`.

When adding a new endpoint that must be public, add it to the
`authorizeHttpRequests` block in `SecurityConfig`. When adding a protected
endpoint, rely on the existing filter — do not add custom filters.

### Naming Conventions

| Layer | Suffix | Example |
|---|---|---|
| Controller | `Controller` | `ProductController` |
| Service | `Service` | `ProductService` |
| Repository (PostgreSQL) | `Access` | `ProductAccess` |
| Repository (MongoDB) | `Access` | `ReviewAccess` |
| JPA Model / DB Model | `Model` | `ProductModel` |
| Auth/relational entities | no suffix | `Customer`, `SystemRole` |
| Request DTO | `Request` | `CreateProductRequest` |
| Response DTO | `Response` | `ProductResponse` |
| Enums | `Status` or `Role` | `OrderStatus`, `UserRole` |

Always use Lombok (`@RequiredArgsConstructor`, `@Data`, `@Getter`, `@Setter`)
to reduce boilerplate. Do not write manual constructors or getters/setters
unless Lombok cannot handle the case.

### Configuration

All sensitive values come from environment variables injected via Docker
Compose. Never hardcode secrets or connection strings. Reference them with
`${ENV_VAR_NAME}` in `application.yml` or `@Value("${...}")` in Java.

App-specific config lives under the `app:` namespace in `application.yml`
(e.g. `app.auth.jwt-secret`, `app.auth.accessTtlSeconds`).

---

## Frontend Architecture

### Stack

React 19 with TypeScript. No UI component library — all styling is custom CSS
(`src/shared/styles/global.css`). Routing via React Router v7
(`createBrowserRouter`).

### Directory Structure

```
src/
├── app/
│   ├── App.tsx         # Root component
│   ├── providers.tsx   # Context providers wrapper
│   └── router.tsx      # All routes defined here
└── shared/
    ├── api/            # All HTTP calls live here
    │   ├── base-api.ts # Central fetch wrapper
    │   ├── authApi.ts
    │   └── productApi.ts
    ├── auth/
    │   └── AuthContext.tsx   # Auth state + login/register/logout
    ├── components/     # Reusable components (ProductCard, ProtectedRoute, etc.)
    ├── layout/         # Layout components (HomeLayout)
    └── pages/          # Page-level components
```

### API Calls — Critical

ALL HTTP requests must go through `baseApi` in `src/shared/api/base-api.ts`.
Do NOT use axios, React Query, SWR, or any other HTTP library — the project
uses native `fetch` only.

The base URL is `http://localhost:8080/api`. The token is read from
`localStorage` under the key `turtleshop_token` and attached as
`Authorization: Bearer <token>` automatically by `baseApi.request`.

```typescript
// Correct — use baseApi helpers
const products = await baseApi.get<ProductResponse[]>("/products");
const result = await baseApi.post<AuthResponse>("/auth/login", credentials);

// Wrong — never call fetch directly in a component or service
const res = await fetch("http://localhost:8080/api/products");
```

### Auth State

Auth state lives in `AuthContext` (`src/shared/auth/AuthContext.tsx`).
The context exposes: `user`, `isAuthenticated`, `isLoading`, `login`,
`register`, `logout`.

Access it with the `useAuth()` hook — never read `localStorage` directly in
components to determine auth state.

```typescript
const { user, isAuthenticated, login, logout } = useAuth();
```

Token and user are persisted to localStorage under:
- `turtleshop_token`
- `turtleshop_user`

### Protected Routes

Wrap any route that requires authentication with `<ProtectedRoute>` in
`router.tsx`. Do not implement custom redirect logic in individual page
components.

### TypeScript

Strict TypeScript is enabled. Always type API responses explicitly. Define
request/response types in the relevant `*Api.ts` file or co-located with the
component if truly local. Do not use `any` unless absolutely unavoidable, and
add a comment explaining why.

---

## Planned Features (Do Not Hallucinate Implementations)

The following features are planned but **not yet implemented**. Copilot should
be aware these are coming but must not suggest stub implementations or assume
any existing infrastructure for them unless files for them already exist.

### 1. Database Optimisation — Indexing & Security
- PostgreSQL index strategy for high-traffic queries (products, orders, cart lookups)
- ACID compliance enforcement at service layer
- Row-Level Security (RLS) policies on sensitive tables (customer data, orders, transactions)
- CIA triad (Confidentiality, Integrity, Availability) hardening

### 2. Redis — Caching Layer
- Redis will be added to Docker Compose as a new service
- Primary use case: caching popular/trending products (high read, low write)
- Do NOT suggest Spring Cache `@Cacheable` patterns yet — the Redis integration
  module and cache-aside strategy are not decided. Wait for the Redis module to
  be scaffolded before suggesting cache annotations.

### 3. Graph Database — Recommendation Engine
- A graph database (likely Neo4j) will be introduced alongside PostgreSQL and
  MongoDB for product recommendations
- Will model customer–product–purchase relationships as a graph
- Do NOT suggest MongoDB aggregation pipelines or SQL joins as a substitute for
  the recommendation engine.

---



## Copilot Behaviour & Database Rework Guidance

GitHub Copilot is expected to actively generate production-ready code when asked
to implement features, fixes, or architectural changes. Generated solutions
should align with the existing project structure, naming conventions, security
model, and database strategy already defined in this document.

### Code Generation Expectations

When implementing a new feature, Copilot should generate a complete vertical
slice whenever appropriate, including:
- Controller
- Service
- Repository / `*Access`
- DTOs (`*Request` / `*Response`)
- Models
- Flyway migration(s)
- Validation
- Security integration
- Frontend API integration where applicable

Generated code should be implementation-ready rather than placeholder or
pseudo-code scaffolding.

### Proactive Engineering Suggestions

Copilot should proactively suggest architectural and database improvements when
relevant, including:
- Missing or inefficient indexes
- Query optimization opportunities
- Transaction boundary improvements
- Security hardening opportunities
- Redis caching opportunities
- Schema normalization or denormalization tradeoffs
- Potential Row-Level Security (RLS) policies
- Graph relationship modeling opportunities for recommendations

Suggestions should remain aligned with the project's existing architecture and
should not introduce conflicting frameworks or patterns.

### Database Rework Standards

When reworking or improving the database layer, base improvements on the
workshops and best practices from the following repository:

- https://github.com/hogeschool/Data-Technologies-public/tree/main/Workshops

The following workshop topics are especially relevant and should guide database
design decisions and implementation details:

#### PostgreSQL Optimization
- B-tree indexes
- Composite indexes
- Partial indexes
- Query optimization
- Materialized views
- Partitioning strategies
- Full-text search

#### Security & Data Protection
- Row-Level Security (RLS)
- Access control patterns
- Privacy and anonymization
- PostgreSQL privilege management

#### Transactions & Consistency
- ACID transaction handling
- Transaction isolation awareness
- Proper rollback and consistency guarantees

#### Graph Database Patterns
- Neo4j relationship modeling
- Graph query design
- Recommendation graph structures

#### Redis Caching Patterns
- Cache-aside strategies
- Read-heavy optimization patterns
- Key/value modeling approaches

When suggesting improvements or generating migrations/query changes, Copilot
should prefer approaches consistent with the workshop material above.

## What Copilot Should Never Do

- Do not suggest Spring Data JPA, `@Entity`, Hibernate, or `CrudRepository`
  for PostgreSQL modules — the project uses raw JDBC intentionally.
- Do not suggest `HttpSession`, session-based auth, or form login — the app is
  fully stateless JWT.
- Do not suggest axios, React Query, SWR, or any fetch library — use `baseApi`.
- Do not add new public endpoints without updating `SecurityConfig`.
- Do not write Flyway migrations inline — always create a new `V{n}__*.sql` file.
- Do not use `@Autowired` field injection — use constructor injection (Lombok
  `@RequiredArgsConstructor`).
- Do not read `localStorage` directly in React components for auth — use `useAuth()`.
- Do not invent new naming conventions — follow the `*Access`, `*Service`,
  `*Controller`, `*Model`, `*Request`, `*Response` pattern strictly.