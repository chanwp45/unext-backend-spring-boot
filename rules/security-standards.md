# Security Standards

## OWASP Top 10 — Backend Controls

| OWASP Risk | Control Required |
|------------|-----------------|
| A01 Broken Access Control | RBAC on every endpoint; ownership checks in service layer |
| A02 Cryptographic Failures | HTTPS only; bcrypt for passwords; AES-256 for sensitive data at rest |
| A03 Injection | Parameterized queries; DTO validation; never eval/exec user input |
| A04 Insecure Design | Threat model per feature; principle of least privilege |
| A05 Security Misconfiguration | No default credentials; disable unused endpoints; Helmet headers |
| A06 Vulnerable Components | Dependabot / Renovate; weekly `npm audit` / OWASP Dependency-Check |
| A07 Auth Failures | Short-lived JWTs; refresh token rotation; account lockout |
| A08 Integrity Failures | Signed JWTs; verify webhook signatures |
| A09 Logging Failures | Structured logs for all auth events; never log secrets |
| A10 SSRF | Whitelist outbound HTTP targets; validate URLs from user input |

---

## Authentication

### JWT Strategy

```typescript
// Access token: short-lived (15 min)
// Refresh token: long-lived (7 days), stored in HttpOnly cookie or DB

interface JwtPayload {
  sub: string;       // userId
  email: string;
  role: UserRole;
  iat: number;
  exp: number;
}

// NestJS JWT config
JwtModule.registerAsync({
  useFactory: (config: ConfigService) => ({
    secret: config.getOrThrow<string>('JWT_SECRET'),
    signOptions: { expiresIn: config.get('JWT_EXPIRES_IN', '15m') },
  }),
  inject: [ConfigService],
})
```

### Password Hashing

```typescript
// Always use bcrypt with cost factor ≥ 12
import * as bcrypt from 'bcrypt';

const SALT_ROUNDS = 12;

async hashPassword(plain: string): Promise<string> {
  return bcrypt.hash(plain, SALT_ROUNDS);
}

async verifyPassword(plain: string, hash: string): Promise<boolean> {
  return bcrypt.compare(plain, hash);
}
```

```java
// Spring Security — use BCryptPasswordEncoder (strength 12)
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
}
```

### Account Lockout

```typescript
// Lock account after N failed login attempts within a time window
const MAX_ATTEMPTS = 5;
const LOCKOUT_DURATION_MINUTES = 15;
```

---

## Authorization (RBAC)

```typescript
// Define roles as enum
export enum UserRole {
  USER = 'USER',
  ADMIN = 'ADMIN',
  MODERATOR = 'MODERATOR',
}

// NestJS — @Roles() guard on every protected endpoint
@Get('admin/users')
@UseGuards(JwtAuthGuard, RolesGuard)
@Roles(UserRole.ADMIN)
async getAllUsers(): Promise<UserResponseDto[]> { ... }

// Service layer — ownership check (not just role)
async updateUser(requesterId: string, targetId: string, dto: UpdateUserDto): Promise<User> {
  if (requesterId !== targetId && requester.role !== UserRole.ADMIN) {
    throw new ForbiddenException('Cannot update another user\'s profile');
  }
  ...
}
```

---

## Input Validation Rules

- **Whitelist, not blacklist** — validate that input IS what we expect, not that it isn't something bad
- **Validate at the controller boundary** — never trust data past the DTO layer
- **Strip unknown properties** — `whitelist: true, forbidNonWhitelisted: false` in NestJS ValidationPipe
- **Max length on all string inputs** — prevents memory exhaustion
- **Numeric range checks** — positive amounts, reasonable date ranges

```typescript
// NestJS global validation pipe
app.useGlobalPipes(
  new ValidationPipe({
    whitelist: true,           // strip unknown properties
    transform: true,           // auto-transform primitive types
    forbidNonWhitelisted: false,
    validateCustomDecorators: true,
  }),
);
```

---

## Secrets Management

```
# Rules
✅ Secrets from environment variables only (process.env / ConfigService)
✅ Use .env.example with placeholder values — never real secrets
✅ .env files in .gitignore
✅ Production secrets in Vault / AWS Secrets Manager / K8s Secrets
✅ Rotate JWT secrets without downtime (dual-key rotation)

# Forbidden
❌ Hardcoded credentials anywhere in source code
❌ Secrets in Docker image labels or ARGs that end up in layers
❌ Secrets in URL query params (visible in logs)
❌ Logging secret values (even in debug/trace level)
```

---

## HTTP Security Headers

### NestJS (Helmet)

```typescript
import helmet from 'helmet';

app.use(helmet());
app.use(helmet.contentSecurityPolicy({ ... }));
```

### Spring Boot

```yaml
# application.yml
server:
  security:
    headers:
      frame-options: DENY
      content-type-options: nosniff
      xss-protection: "1; mode=block"
```

### .NET

```csharp
app.UseHsts();
app.UseHttpsRedirection();
```

---

## Rate Limiting

```typescript
// NestJS — @nestjs/throttler
ThrottlerModule.forRoot([
  { name: 'short', ttl: 1000, limit: 10 },   // 10 req/sec
  { name: 'long',  ttl: 60000, limit: 100 }, // 100 req/min
]),

// Apply globally or per-endpoint
@UseGuards(ThrottlerGuard)
@Throttle({ default: { ttl: 60000, limit: 5 } })   // strict limit for auth endpoints
@Post('auth/login')
```

---

## Sensitive Data Rules

| Data Type | Rule |
|-----------|------|
| Passwords | Hash with bcrypt ≥ 12 rounds; never log or return |
| PII (email, phone) | Mask in logs (`j***@example.com`); encrypt at rest if regulated |
| Payment data | Never store raw; use PCI-DSS compliant processor (Stripe, etc.) |
| Tokens / API keys | Store hashed (SHA-256); compare hash only |
| Audit logs | Immutable; include user ID, action, timestamp, IP |

---

## Security Checklist (per PR)

- [ ] No secrets or credentials in code diff
- [ ] All new endpoints have auth guard + role check
- [ ] All DTOs have validation decorators / annotations
- [ ] SQL queries use parameterized binding (no string concat)
- [ ] Error responses don't expose stack traces or internal paths
- [ ] New dependencies scanned (`npm audit` / OWASP check)
- [ ] Rate limiting applied to auth & sensitive endpoints
