# SOP Violations — Backend

## Violation Severity Levels

| Level | Definition | Response |
|-------|------------|----------|
| **Minor** | Small deviation, low risk, easy to fix | Comment in PR, fix before merge |
| **Major** | Significant deviation, introduces risk or debt | PR blocked until resolved |
| **Critical** | Security risk, data integrity risk, or production incident risk | Immediate escalation, no merge |

---

## Minor Violations

| Violation | Example | Action |
|-----------|---------|--------|
| Missing OpenAPI annotation on new endpoint | No `@ApiResponse` on controller method | Add before merge |
| Inconsistent naming | `getUserById` vs `findUserById` in same class | Align with team convention |
| Hardcoded magic numbers | `if (attempts > 5)` | Extract to named constant |
| Missing `@IsOptional()` on optional DTO field | Field throws 422 when absent | Add decorator |
| Business logic in controller | Calculation in handler instead of service | Move to service layer |
| Test without AAA structure | Mixed arrange/act/assert | Refactor test |

---

## Major Violations

| Violation | Example | Action |
|-----------|---------|--------|
| Missing auth guard on protected endpoint | `GET /admin/users` has no `@UseGuards` | **Block PR** — add guard |
| DTO missing validation decorators | `password: string` with no `@MinLength` | **Block PR** — add validation |
| Direct DB call in controller | `this.userRepository.find()` in controller | **Block PR** — move to service |
| Raw string concatenation in query | `` `WHERE email = '${email}'` `` | **Block PR** — parameterize |
| Migration without `down()` | Empty or missing rollback function | **Block PR** — implement rollback |
| `any` type used in service/repository | `async process(data: any)` | **Block PR** — type properly |
| Sensitive data logged | `logger.info('Password: ' + password)` | **Block PR** — remove log |

---

## Critical Violations

| Violation | Example | Action |
|-----------|---------|--------|
| Hardcoded secret or credential in code | `const SECRET = 'mypassword123'` | **Immediate escalation** — rotate secret, block PR |
| SQL injection vulnerability | Raw user input in query string | **Immediate escalation** — fix + security review |
| Disabled auth on prod endpoint intentionally | `@Public()` on sensitive admin route | **Immediate escalation** — review with lead |
| Editing an already-applied migration | Modifying a committed migration file | **Immediate escalation** — revert, create new migration |
| Storing plaintext passwords | `user.password = dto.password` (no hash) | **Immediate escalation** — security incident process |
| Committing `.env` file with real secrets | Pushing `.env` to remote | **Immediate escalation** — rotate all secrets immediately |

---

## Escalation Path

```
Minor violation
  └── Author fixes before merge (reviewer comment)

Major violation
  └── PR blocked → Author fixes → Re-review required

Critical violation
  └── PR blocked immediately
  └── Notify team lead + security contact
  └── If secrets committed → rotate all affected credentials NOW
  └── Post-incident review required before re-submission
```

---

## Recurring Violation Policy

If the same team member commits the same **Major** or **Critical** violation more than twice:

1. Schedule 1:1 with team lead to review SOP requirements
2. Pair programming session on the affected area
3. Mandatory review of relevant `rules/` documents
4. Follow-up in next sprint retrospective
