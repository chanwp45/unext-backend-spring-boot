# CLAUDE.md - Dynamic Backend Project AI Code Assistant & SOP Configuration

---

## Project Overview

| Field | Value |
|-------|-------|
| Project Name | unext-backend |
| Description | UNext Platform Backend REST API |
| Framework | Spring Boot 3.3 |
| Language | Java 21 |
| Database | PostgreSQL 16 |
| ORM / Data Layer | JPA / Hibernate + Flyway migrations |
| Auth Strategy | JWT (access 15m + refresh 7d with rotation) |
| Target Users | [TARGET_USERS] |
| Launch Date | [TARGET_LAUNCH_DATE] |

---

## Project Starter Kit Features

- Modern, production-ready backend boilerplate
- Clean Architecture / Domain-Driven Design principles
- RESTful API with OpenAPI (Swagger) documentation
- Built-in authentication & authorization
- Database migration strategy
- Structured logging & error handling
- Health check & observability endpoints
- Docker-ready multi-stage build
- [ADD_CUSTOM_FEATURES_HERE]

---

## Folder Structure

> Pattern: **Module-First (Vertical Slice)** — group by domain, not by layer.  
> Each module is self-contained; `shared/` holds only truly cross-cutting code.

```
[PROJECT_NAME]/
│
├── src/
│   ├── app/                              # App bootstrap & server setup
│   │   ├── app.module.ts                 # Root module (NestJS) / AppConfig (Spring)
│   │   └── main.ts                       # Entry point
│   │
│   ├── modules/                          # Domain modules (vertical slices)
│   │   └── [module-name]/                # e.g. auth/, users/, orders/
│   │       ├── controllers/              # HTTP handlers / route handlers
│   │       │   ├── [module].controller.ts
│   │       │   └── [module].controller.spec.ts
│   │       ├── services/                 # Business logic
│   │       │   ├── [module].service.ts
│   │       │   └── [module].service.spec.ts
│   │       ├── repositories/             # Data access layer
│   │       │   ├── [module].repository.ts
│   │       │   └── [module].repository.spec.ts
│   │       ├── entities/                 # Domain entities / models
│   │       ├── dto/                      # Request & Response DTOs (with validation)
│   │       ├── types/                    # Module-scoped TypeScript types
│   │       ├── guards/                   # Module-level auth guards
│   │       ├── events/                   # Domain events (if event-driven)
│   │       └── index.ts                  # Public API — barrel export
│   │
│   ├── shared/                           # Truly cross-cutting code only
│   │   ├── middleware/                   # HTTP middleware (logging, correlation-id)
│   │   ├── guards/                       # Global auth guards
│   │   ├── decorators/                   # Custom decorators
│   │   ├── filters/                      # Exception filters / error handlers
│   │   ├── interceptors/                 # Logging, transform, timeout interceptors
│   │   ├── pipes/                        # Validation pipes
│   │   ├── utils/                        # Pure utility functions
│   │   ├── types/                        # Global TypeScript interfaces
│   │   └── constants/                    # App-wide constants & enums
│   │
│   ├── config/                           # Configuration (env, feature flags)
│   │   ├── app.config.ts
│   │   ├── database.config.ts
│   │   └── auth.config.ts
│   │
│   └── database/                         # DB setup & migrations
│       ├── migrations/                   # Versioned migration files
│       ├── seeds/                        # Seed data (dev/test)
│       └── data-source.ts                # ORM datasource config
│
├── tests/
│   ├── e2e/                              # End-to-end / API integration tests
│   └── integration/                      # Multi-module integration tests
│
├── docs/                                 # API docs, ADRs, guides
├── rules/                                # SOP rules (see below)
├── scripts/                              # Dev, migration, CI utility scripts
└── .github/
    ├── workflows/
    │   ├── ci.yml                        # Lint, test, build on PR
    │   └── deploy.yml                    # Deploy on merge to main
    └── PULL_REQUEST_TEMPLATE.md
```

### Framework-Specific Variants

#### Spring Boot (Java)
```
src/main/java/com/[org]/[project]/
├── [module]/
│   ├── controller/         # @RestController + @RequestMapping
│   ├── service/            # @Service + @Autowired
│   ├── repository/         # JpaRepository interface (@Repository ไม่ต้องใส่)
│   ├── entity/             # @Entity + @Data + @EqualsAndHashCode
│   ├── dto/                # Java Records (request/response)
│   └── exception/          # Module-specific exceptions
├── shared/
│   ├── config/             # @Configuration + @Bean
│   ├── exception/          # @RestControllerAdvice
│   ├── security/           # @Component + Spring Security
│   └── util/
src/main/resources/
├── application.yml
├── application-dev.yml
└── application-prod.yml
src/test/java/...           # Mirror of main structure
```

#### Spring Boot — Lombok Annotation Pattern
```java
// Controller
@RestController
@RequestMapping("/v1/users")
@Tag(name = "users")
public class UserController {
    @Autowired private UserService userService;
}

// Service
@Service
public class UserService {
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
}

// Repository — ไม่ต้อง implement, Spring Data JPA สร้างให้
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
}

// Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;
}

// Logging
@Slf4j
@Component
public class SomeComponent {
    // ใช้ log.info(), log.debug(), log.error() ได้เลย
}
```

#### .NET (C#)
```
[ProjectName]/
├── [Module]/
│   ├── Controllers/        # [ApiController]
│   ├── Services/           # IService + implementation
│   ├── Repositories/       # IRepository + EF Core implementation
│   ├── Models/             # Domain entities
│   └── DTOs/               # Request/Response records
├── Shared/
│   ├── Middleware/
│   ├── Extensions/
│   └── Exceptions/
├── Infrastructure/
│   ├── Data/               # DbContext, migrations
│   └── Auth/
└── Program.cs
```

### Architecture Rules

| Rule | Description |
|------|-------------|
| Module boundary | Modules must **not** import from each other directly — use shared services or events |
| Layer flow | Controller → Service → Repository (never skip or reverse) |
| DTO validation | All request DTOs must have validation decorators / annotations |
| No business logic in controllers | Controllers only parse input, delegate to service, return response |
| Repository abstraction | Business logic must not know about the ORM/DB technology |
| Secrets in env only | No hardcoded credentials, API keys, or connection strings in code |

### Spring Boot Annotation Rules

| Layer | Required Annotations | Notes |
|-------|---------------------|-------|
| Controller | `@RestController` `@RequestMapping` | inject ด้วย `@Autowired` |
| Service | `@Service` | inject ด้วย `@Autowired` |
| Repository | extends `JpaRepository` | ไม่ต้องใส่ `@Repository` |
| Entity | `@Entity` `@Data` `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` | ต้องใส่ `@EqualsAndHashCode.Include` บน `@Id` field |
| Config | `@Configuration` `@Bean` | inject ด้วย `@Autowired` |
| Filter/Component | `@Component` | inject ด้วย `@Autowired` |
| Logging | `@Slf4j` | ใช้ `log.info/debug/error()` |

---

## API Endpoint Generation

> เมื่อถูกขอให้สร้าง API endpoint ให้อ่าน [`rules/api-design.md`](./rules/api-design.md) และ generate ไฟล์ต่อไปนี้ทันที:
> - `[module].controller.ts` — HTTP handlers พร้อม OpenAPI decorators + input validation
> - `[module].service.ts` — Business logic พร้อม error handling ที่ถูกต้อง
> - `[module].repository.ts` — Data access layer (abstracted จาก business logic)
> - `dto/` — Request/Response DTOs พร้อม validation + Swagger schema
> - `[module].controller.spec.ts` — Unit tests ครอบคลุม happy path / error / edge cases
> - `[module].service.spec.ts` — Service unit tests พร้อม mocked dependencies
>
> ห้ามแสดงแค่ interface spec — ต้อง generate code จริงเสมอ

---

## Database Migration Generation

> เมื่อถูกขอให้สร้าง migration ให้อ่าน [`rules/database-standards.md`](./rules/database-standards.md) และ generate:
> - Migration file พร้อม `up()` และ `down()` ที่ reversible เสมอ
> - Entity / Model update ที่สอดคล้องกับ migration
> - Index strategy สำหรับ columns ที่ query บ่อย
>
> ห้ามแก้ migration เก่า — ต้อง create migration ใหม่เสมอ

---

## SOP Rules

All standards are broken out into individual rule files under [`rules/`](./rules/):

| File | Description |
|------|-------------|
| [ai-tools-config.md](./rules/ai-tools-config.md) | AI capabilities, backend stack, recommended prompt format |
| [coding-standards.md](./rules/coding-standards.md) | Naming, formatting, architecture rules per framework |
| [unit-test-standard.md](./rules/unit-test-standard.md) | Test pattern, stack, coverage goals (unit / integration / contract) |
| [api-design.md](./rules/api-design.md) | REST API design, versioning, response format, error codes |
| [database-standards.md](./rules/database-standards.md) | Migration strategy, ORM patterns, index & query rules |
| [security-standards.md](./rules/security-standards.md) | Auth, OWASP, input validation, secrets management |
| [git-workflow.md](./rules/git-workflow.md) | Branch naming, workflow steps, PR template, code review checklist |
| [commit-convention.md](./rules/commit-convention.md) | Commit message format and types |
| [definition-of-done.md](./rules/definition-of-done.md) | DoD checklist for every task/story |
| [sop-violations.md](./rules/sop-violations.md) | Minor/major violations and escalation path |
| [documentation-standards.md](./rules/documentation-standards.md) | Required docs, OpenAPI/Swagger standards |
| [team-guidelines.md](./rules/team-guidelines.md) | Mandatory requirements, onboarding checklist |
| [docker.md](./rules/docker.md) | Dockerfile, docker-compose, health checks, build script |

---

## Docker Build

> เมื่อถูกขอให้สร้าง Dockerfile หรือ Docker config ให้อ่าน [`rules/docker.md`](./rules/docker.md) และ generate ไฟล์ต่อไปนี้ทันที:
> - `Dockerfile` — multi-stage build (builder → runtime image)
> - `.dockerignore` — exclude build artifacts, secrets, test files
> - `docker-compose.yml` — dev profile (hot-reload + DB) + prod profile
> - `scripts/docker-build.sh` — build, tag, optional push
>
> ห้ามแสดงแค่ตัวอย่าง — ต้อง generate code จริงเสมอ

### Docker Quick Commands

```bash
# NestJS / Node.ts
npm run docker:dev       # docker compose --profile dev up
npm run docker:build     # docker compose --profile prod build
npm run docker:prod      # docker compose --profile prod up

# Spring Boot / .NET — same pattern via docker compose
docker compose --profile dev up
docker compose --profile prod up --build
```

---

## Environment Variables (.env.example)

```
# App
APP_PORT=3000
APP_ENV=development
APP_LOG_LEVEL=info

# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=[PROJECT_NAME]_db
DB_USER=postgres
DB_PASSWORD=changeme

# Auth
JWT_SECRET=changeme-use-strong-secret-in-prod
JWT_EXPIRES_IN=15m
JWT_REFRESH_EXPIRES_IN=7d

# External Services
[SERVICE_NAME]_API_URL=https://api.example.com
[SERVICE_NAME]_API_KEY=changeme

# Observability
SENTRY_DSN=
OTEL_EXPORTER_OTLP_ENDPOINT=
```

---

## Quick Start — unext-backend (Spring Boot)

### 1. Prerequisites

| Tool | Version | Notes |
|------|---------|-------|
| Java (JDK) | 17+ | `JAVA_HOME` must be set |
| PostgreSQL | 16 | Local or via Docker |
| Maven | — | **Bundled via `mvnw` wrapper** (auto-downloads on first run) |

> Maven ไม่จำเป็นต้องติดตั้งแยก — `mvnw` / `mvnw.cmd` จะ download Maven 3.9.6 ให้อัตโนมัติครั้งแรก

---

### 2. Environment Setup

```bash
# คัดลอก env template
cp .env.example .env

# แก้ไขค่าใน .env ให้ตรงกับ local database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=unext_db
DB_USER=postgres
DB_PASSWORD=yourpassword
JWT_SECRET=your-strong-secret-min-32-chars
```

---

### 3. Maven Wrapper Commands (Windows)

```cmd
# ครั้งแรก — wrapper จะ download Maven อัตโนมัติ
mvnw.cmd spring-boot:run

# หรือ compile + run
mvnw.cmd compile
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=development

# Build JAR (skip tests)
mvnw.cmd package -DskipTests

# Run tests
mvnw.cmd test

# Full build + test
mvnw.cmd verify

# Run only a specific test class
mvnw.cmd test -Dtest=UserServiceTest
```

### 3b. Maven Wrapper Commands (Linux / macOS)

```bash
chmod +x mvnw       # ครั้งแรกเท่านั้น

./mvnw spring-boot:run
./mvnw compile
./mvnw package -DskipTests
./mvnw test
./mvnw verify
```

---

### 4. Run with Docker (Recommended)

```bash
# Dev mode — hot-reload + PostgreSQL
docker compose --profile dev up

# Production build + run
docker compose --profile prod up --build

# Build image only
bash scripts/docker-build.sh
# หรือบน Windows:
# IMAGE_NAME=unext-backend TAG=v1.0.0 bash scripts/docker-build.sh
```

---

### 5. API Base URL & Swagger

| | URL |
|---|---|
| API Base | `http://localhost:8080/api` |
| Swagger UI | `http://localhost:8080/api/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/api/v3/api-docs` |
| Health Check | `http://localhost:8080/api/actuator/health` |

---

### 6. Flyway Migrations

Migrations รันอัตโนมัติตอน app start  
ไฟล์อยู่ใน `src/main/resources/db/migration/`

| Version | Description |
|---------|-------------|
| V1 | Create users table |
| V2 | Create refresh_tokens table |
| V3 | Add STAFF, STUDENT roles |
| V4 | Create faculties & departments tables |
| V5 | Create curricula table |
| V6 | Create students table |
| V7 | Create audit_logs table |
| V8 | Seed faculties & departments data |

---

### NestJS / Node TypeScript (template reference)
```bash
npm install             # Install dependencies
npm run start:dev       # Development server (hot-reload)
npm run test            # Unit tests
npm run test:cov        # Coverage report
npm run test:e2e        # End-to-end tests
npm run lint            # ESLint check
npm run build           # Production build
npm run migration:run   # Run pending migrations
npm run migration:revert # Revert last migration
```

### .NET (template reference)
```bash
dotnet run              # Development server
dotnet test             # Run all tests
dotnet build            # Build
dotnet ef migrations add [Name]   # Create migration
dotnet ef database update         # Apply migrations
```

---

## Development Goals & Metrics

| Metric | Target |
|--------|--------|
| Template & SOP Coverage | 100% |
| Team Adoption Rate | ≥ 80% |
| Unit Test Coverage | ≥ 80% |
| Integration Test Coverage | Critical API flows |
| Code Quality | A grade (linter/static analysis) |
| Build Success Rate | 99%+ |
| PR Review Time | < 24 hours |
| API Response Time (p95) | < 200ms |
| Error Rate | < 0.1% |
| Security Scan | 0 Critical / High CVEs |

---

## Resources

| Category | Link |
|----------|------|
| NestJS | https://docs.nestjs.com/ |
| Spring Boot | https://docs.spring.io/spring-boot/docs/current/reference/html/ |
| .NET | https://learn.microsoft.com/en-us/aspnet/core/ |
| OpenAPI Spec | https://swagger.io/specification/ |
| OWASP Top 10 | https://owasp.org/www-project-top-ten/ |
| Conventional Commits | https://www.conventionalcommits.org/ |
| Twelve-Factor App | https://12factor.net/ |

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | May 2026 | Foundation Release - Initial backend template |

---

**Last Updated**: May 2026 | **Version**: 1.0 | **Status**: Active  
**Maintained By**: [TEAM_NAME] / [MAINTAINER_NAME] | **Next Review**: [DATE]
