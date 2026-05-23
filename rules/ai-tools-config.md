# AI Tools Configuration — Backend

## Available Capabilities

- REST API endpoint scaffolding (Controller / Service / Repository)
- DTO generation with validation decorators
- Database migration & entity/model creation
- Unit & integration test generation
- OpenAPI / Swagger annotation
- Authentication & authorization implementation (JWT, OAuth2, RBAC)
- Error handling & exception filter patterns
- Middleware & interceptor creation
- Query optimization suggestions
- Security review (OWASP Top 10)
- Dockerfile & CI/CD pipeline generation
- [ADD_CUSTOM_CAPABILITY]

---

## Stack

| Category | NestJS / Node TS | Spring Boot (Java) | .NET (C#) |
|----------|------------------|--------------------|-----------|
| Language | TypeScript (strict) | Java 21+ | C# 12+ / .NET 8+ |
| Framework | NestJS 10+ | Spring Boot 3.x | ASP.NET Core 8 |
| Build Tool | tsc + esbuild | Maven / Gradle | dotnet CLI |
| ORM | TypeORM / Prisma / Drizzle | JPA + Hibernate | Entity Framework Core |
| Validation | class-validator + class-transformer | Bean Validation (Jakarta) | FluentValidation / DataAnnotations |
| Auth | @nestjs/jwt + Passport | Spring Security | ASP.NET Core Identity / JWT Bearer |
| API Docs | @nestjs/swagger | springdoc-openapi | Swashbuckle / NSwag |
| Unit Testing | Jest + Supertest | JUnit 5 + Mockito | xUnit / NUnit + Moq |
| Integration Test | Jest + TestContainers | Spring Boot Test + TestContainers | WebApplicationFactory + TestContainers |
| DB Migration | TypeORM migrations / Prisma migrate | Flyway / Liquibase | EF Core Migrations |
| Linter | ESLint (flat config v9+) | Checkstyle + PMD | Roslyn Analyzers |
| Formatter | Prettier | google-java-format | dotnet-format |
| Package Manager | pnpm / npm | Maven / Gradle | NuGet |

---

## Recommended Prompt Format

```markdown
## Task: [Clear Description]
## Framework: [Spring Boot / NestJS / .NET / Node TS]
## Context: [Module, existing code, data model background]
## Requirements:
- [ ] Requirement 1
- [ ] Requirement 2
## Constraints:
- DB: [Database type & ORM]
- Auth: [Auth strategy — JWT / OAuth2 / API Key]
- Performance: [Expected load / SLA]
- Security: OWASP Top 10 compliance
## Expected Output:
- [ ] Controller with validation + OpenAPI decorators
- [ ] Service with business logic + error handling
- [ ] Repository with typed queries
- [ ] DTOs (request + response)
- [ ] Unit tests (≥ 80% coverage)
```

---

## Code Generation Rules

1. **Always generate complete files** — no partial stubs or TODO placeholders
2. **DTOs must have validation** — never accept raw `any` / `Object` from HTTP input
3. **Services must be stateless** — no instance-level mutable state
4. **Errors must be typed** — use domain exceptions, not generic `Error` throws
5. **Secrets from env only** — never hardcode credentials in generated code
6. **Tests must mock at boundaries** — mock repository layer, not service internals
7. **OpenAPI must be complete** — every endpoint needs `@ApiResponse` for 2xx and error codes

---

## Spring Boot — Required Annotation Pattern

เมื่อ generate code สำหรับ Spring Boot ต้องใช้ annotation ต่อไปนี้เสมอ:

### Dependency Injection
```java
// ✅ ใช้ @Autowired บน field เสมอ
@Service
public class UserService {
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
}

// ❌ ห้ามเขียน constructor inject ด้วยมือ
// ❌ ห้ามใช้ @RequiredArgsConstructor
```

### Entity
```java
// ✅ ใช้ @Data + @EqualsAndHashCode เสมอ
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include   // ← ต้องมีเสมอบน @Id
    private UUID id;
}

// ❌ ห้ามเขียน getter/setter เอง
// ❌ ห้ามใช้ @Getter @Setter แยก
```

### Logging
```java
// ✅ ใช้ @Slf4j เสมอ
@Slf4j
@Service
public class AuthService {
    // ใช้ log.info(), log.debug(), log.warn(), log.error()
}

// ❌ ห้ามประกาศ Logger ด้วยมือ
// private static final Logger log = LoggerFactory.getLogger(...);
```

### Annotation Summary Table

| Class Type | Annotations ที่ต้องใส่ |
|------------|----------------------|
| Controller | `@RestController` `@RequestMapping("/v1/...")` `@Tag` |
| Service | `@Service` |
| Repository | Interface extends `JpaRepository<Entity, IdType>` |
| Entity | `@Data` `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` `@Entity` `@Table` |
| Config | `@Configuration` |
| Security Filter | `@Component` |
| Logging (any class) | `@Slf4j` |
| Field injection | `@Autowired` |
| Primary Key | `@Id` `@EqualsAndHashCode.Include` |
