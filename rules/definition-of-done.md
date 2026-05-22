# Definition of Done (DoD) — Backend

A user story or task is considered **complete** only when ALL items below are checked:

## Code Quality
- [ ] Code written and peer-reviewed
- [ ] Follows naming and architecture conventions ([Coding Standards](./coding-standards.md))
- [ ] No linter errors or warnings (ESLint / Checkstyle / Roslyn)
- [ ] No `any` types (TS) / raw `Object` bindings / unchecked casts (Java)
- [ ] No hardcoded credentials, secrets, or magic strings
- [ ] Dead code removed

## Testing
- [ ] Unit tests written for all new service and repository methods
- [ ] Unit test coverage ≥ 80% on new/changed code
- [ ] Integration or E2E test added for new API endpoints
- [ ] All tests pass in CI (`npm test` / `mvn verify` / `dotnet test`)
- [ ] No skipped or disabled tests without documented reason

## API & Documentation
- [ ] OpenAPI / Swagger annotations complete on all new endpoints
- [ ] Request DTOs have full validation (class-validator / Bean Validation / FluentValidation)
- [ ] Error responses use domain exception codes (not raw HTTP status only)
- [ ] `.env.example` updated if new environment variables added

## Database
- [ ] Migration file created (if schema changed)
- [ ] Migration `down()` function verified locally
- [ ] New indexes added for columns used in WHERE / ORDER BY
- [ ] No breaking schema changes without a versioned migration plan

## Security
- [ ] All new endpoints protected with appropriate auth guard + role
- [ ] Input validation rejects unexpected fields
- [ ] No secrets or PII in logs
- [ ] `npm audit` / OWASP Dependency-Check: 0 Critical or High CVEs

## Operational Readiness
- [ ] Structured log messages added for key business events
- [ ] Health check still passes after changes
- [ ] Docker build succeeds (`docker compose --profile prod build`)
- [ ] Environment variables documented

## Process
- [ ] Pull Request created with completed PR template
- [ ] PR approved by ≥ 1 reviewer
- [ ] CI pipeline green (lint + test + build + security scan)
- [ ] Merged to main branch
- [ ] Branch deleted after merge
