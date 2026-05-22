# Commit Message Convention

## Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

## Commit Types

| Type | Description |
|------|-------------|
| `feat` | A new feature (API endpoint, service method, module) |
| `fix` | A bug fix |
| `docs` | Documentation only (Swagger, README, ADR) |
| `style` | Formatting, missing semicolons — no logic change |
| `refactor` | Code change without feature or bug change |
| `perf` | Performance improvement (query optimization, caching) |
| `test` | Adding or updating tests |
| `chore` | Build process, dependency updates, CI/CD changes |
| `migration` | Database migration (backend-specific type) |
| `security` | Security fix or hardening |

## Scope Examples (Backend)

```
feat(users): add endpoint to get user profile
feat(auth): implement refresh token rotation
fix(orders): correct total calculation on partial refund
migration(users): add deleted_at column for soft delete
perf(products): add composite index on category_id and status
security(auth): enforce account lockout after 5 failed attempts
test(users): add integration tests for user creation flow
chore(deps): upgrade nestjs to 10.4.0
```

## Examples

```
feat(auth): add JWT refresh token endpoint

Implements POST /v1/auth/refresh-token. Refresh tokens are stored
hashed in DB and rotated on each use (single-use strategy).

Closes #42

---

migration(orders): create orders and order_items tables

Adds versioned migration with up() and down(). Includes indexes on
user_id and status columns for common query patterns.

---

fix(users): return 404 instead of 500 when user not found

UserService.findById was throwing unhandled TypeError when
repository returned null. Now throws UserNotFoundException.

Fixes #87
```

## Rules

- Subject line ≤ 72 characters
- Subject in imperative mood: "add" not "added" / "adds"
- No period at end of subject line
- Body explains WHY, not WHAT (the diff shows what)
- Reference issue numbers in footer (`Closes #123`, `Fixes #87`)
