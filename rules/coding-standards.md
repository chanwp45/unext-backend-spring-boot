# Coding Standards — Backend

## Naming Conventions

### NestJS / TypeScript

```typescript
// Classes (PascalCase)
class UserService {}
class CreateUserDto {}
class UserNotFoundException extends HttpException {}

// Methods & variables (camelCase)
async findUserById(id: string): Promise<User> {}
const isEmailVerified = false;

// Constants (UPPER_SNAKE_CASE)
const MAX_LOGIN_ATTEMPTS = 5;
const JWT_EXPIRES_IN = '15m';

// Files (kebab-case)
// user.service.ts  |  create-user.dto.ts  |  auth.guard.ts

// Interfaces — prefix with I only for repository contracts
interface IUserRepository {}
interface UserPayload {}         // no prefix for plain shapes
```

### Spring Boot / Java

```java
// Classes (PascalCase)
@RestController
public class UserController {}

@Service
public class UserService {}

@Entity
public class User {}

// Methods & variables (camelCase)
public UserDto findUserById(Long id) {}
private boolean isEmailVerified;

// Constants (UPPER_SNAKE_CASE in enum or static final)
public static final int MAX_LOGIN_ATTEMPTS = 5;

// Packages (lowercase)
com.example.project.user.controller
com.example.project.user.service
```

### .NET / C#

```csharp
// Classes, methods, properties (PascalCase)
public class UserService : IUserService {}
public async Task<UserDto> GetUserByIdAsync(Guid id) {}
public string FirstName { get; init; }

// Local variables & parameters (camelCase)
var isEmailVerified = false;
string userId = ...;

// Constants (PascalCase for public, UPPER_SNAKE for private)
public const int MaxLoginAttempts = 5;
private const string JwtIssuer = "...";

// Files match class name exactly
// UserService.cs | CreateUserRequest.cs | UserController.cs
```

---

## Formatting Standards

| Rule | NestJS / TS | Spring / Java | .NET / C# |
|------|-------------|---------------|-----------|
| Indentation | 2 spaces | 4 spaces | 4 spaces |
| Line Length | Max 120 chars | Max 120 chars | Max 120 chars |
| Formatter | Prettier | google-java-format | dotnet-format |
| Trailing Commas | ES5 | N/A | N/A |

---

## Architecture Rules (All Frameworks)

```
HTTP Layer (Controller / RestController / ApiController)
    ↓  parse + validate input, delegate, return response
Service Layer
    ↓  business logic, domain rules, orchestration
Repository Layer
    ↓  data access only, no business logic
Database
```

### Layer Responsibilities

| Layer | Allowed | Forbidden |
|-------|---------|-----------|
| Controller | Input parsing, validation, auth guards, response mapping | Business logic, DB calls |
| Service | Business rules, domain validation, cross-module orchestration | Direct DB queries, HTTP details |
| Repository | CRUD queries, transactions, raw SQL | Business logic |
| Entity/Model | Field definitions, simple computed properties | Service calls, HTTP context |

### Key Rules

- **One responsibility per class** — split large services into focused sub-services
- **DTO for every input** — never bind HTTP params directly to entities/models
- **Typed errors** — throw domain exceptions (`UserNotFoundException`), not generic messages
- **No circular dependencies** — use events or shared services to decouple modules
- **Repository interface** — define `IUserRepository` interface; bind implementation via DI
- **Immutable DTOs** — response DTOs should be read-only (Java records, C# `init`, TS `readonly`)

---

## TypeScript-Specific Rules (NestJS)

```typescript
// strict mode required in tsconfig.json
{
  "compilerOptions": {
    "strict": true,
    "noImplicitAny": true,
    "strictNullChecks": true
  }
}
```

- No `any` — use `unknown` and type-narrow, or define proper types
- All exported functions must have explicit return types
- Use `readonly` on DTO and entity properties where mutation is not needed
- Prefer `class` for DTOs (enables class-validator decoration)
- Use `enum` for status/type fields — not bare string literals

---

## Java-Specific Rules (Spring Boot)

- Use Java Records for immutable DTOs where possible (Java 16+)
- Mark service classes `final` unless extension is explicitly needed
- Use constructor injection only — no `@Autowired` on fields
- Use `Optional<T>` at repository return types — never return `null`
- All public API methods in interfaces must have Javadoc

---

## C#-Specific Rules (.NET)

- Use `record` for immutable request/response DTOs
- Use `required` modifier on mandatory DTO properties (C# 11+)
- Prefer `IAsyncEnumerable<T>` for streaming endpoints
- Use `CancellationToken` on all async controller/service methods
- Use nullable reference types (`#nullable enable`) project-wide
