# Team Guidelines — Backend

## Mandatory Requirements (ต้องปฏิบัติ)

- All code must follow **Naming & Architecture Conventions** → [Coding Standards](./coding-standards.md)
- All commits must follow **Commit Message Convention** → [Commit Convention](./commit-convention.md)
- All new services and repositories must have **unit tests** (≥ 80% coverage) → [Unit Test Standard](./unit-test-standard.md)
- All PRs must pass **Definition of Done** checklist → [Definition of Done](./definition-of-done.md)
- All new API endpoints must have **OpenAPI annotations** → [API Design](./api-design.md)
- All input DTOs must have **validation decorators / annotations**
- All new DB schema changes require a **versioned migration** → [Database Standards](./database-standards.md)
- All endpoints serving sensitive data must have **auth guard + RBAC** → [Security Standards](./security-standards.md)
- **No secrets in source code** — environment variables only

---

## For New Projects

1. Copy `CLAUDE.md` to your project root
2. Copy the `rules/` folder to your project
3. Replace all `[PLACEHOLDER]` values with actual project details
4. Choose your framework section and remove irrelevant variants
5. Commit to version control on `main` before first feature branch
6. Reference `CLAUDE.md` in `README.md` for team onboarding

---

## For Existing Projects

1. Create or update `CLAUDE.md` in project root
2. Audit current codebase against SOP rules — document deviations with reason
3. Add missing test coverage incrementally (don't block delivery)
4. Share with team, align on standards, set adoption timeline
5. Update quarterly or when standards change

---

## Onboarding Checklist (New Developer)

- [ ] Clone repo and run locally (follow `docs/local-development.md`)
- [ ] Run all tests — they should all pass
- [ ] Read `CLAUDE.md` and all `rules/` files
- [ ] Read latest ADRs in `docs/architecture-decisions/`
- [ ] Explore Swagger UI (`/api-docs`) and understand the API structure
- [ ] Run DB migrations and seed data
- [ ] Create a test branch, make a small change, open a draft PR — verify CI passes
- [ ] Pair with a team member on the first real task

---

## Framework Selection Guide

Use this to choose your backend framework when starting a new project:

| Use Case | Recommended |
|----------|-------------|
| Microservice, Node.js team, TypeScript-first | **NestJS** |
| Enterprise Java, Spring ecosystem, JVM team | **Spring Boot** |
| .NET / C# team, Windows or Azure-first | **.NET ASP.NET Core** |
| Lightweight Node API, no framework overhead | **Fastify + TypeScript** |
| Python team, ML-adjacent or data APIs | **FastAPI** |

---

## Code Review Culture

- **Review the code, not the person** — feedback is about the change, not the author
- **Approve fast** — target < 24 hours first response on PR
- **Blocking vs. non-blocking** — use `[blocking]` prefix for must-fix, `[nit]` for optional
- **Explain, don't dictate** — link to SOP rules when requesting changes
- **Author merges** — reviewer approves, author decides when to merge (after CI green)
