# Git Workflow

## Branch Naming Convention

```
feature/[feature-name]             # New features
bugfix/[bug-name]                  # Bug fixes
hotfix/[hotfix-name]               # Production hotfixes (branch from main/tag)
docs/[documentation-update]        # Documentation only
refactor/[refactor-name]           # Code refactoring (no behavior change)
chore/[task-name]                  # Maintenance (deps, CI, tooling)
migration/[migration-description]  # Database migrations (backend-specific)
```

## Workflow Steps

1. Create feature branch from `main`
2. Make commits following the [Commit Convention](./commit-convention.md)
3. Write / update unit tests before pushing
4. Push branch to remote
5. Create Pull Request using the template below
6. Pass CI (lint + test + build + security scan)
7. Code review & approval (minimum 1 reviewer)
8. Merge and delete branch

## Pull Request Template

```markdown
## Description
Brief description of the change and motivation.

## Type of Change
- [ ] New Feature (API endpoint, service, module)
- [ ] Bug Fix
- [ ] Database Migration
- [ ] Refactoring (no behavior change)
- [ ] Performance Improvement
- [ ] Security Fix
- [ ] Documentation
- [ ] CI/CD / Infrastructure

## API Changes
- [ ] New endpoints added (list them)
- [ ] Existing endpoint signature changed (breaking?)
- [ ] No API changes

## Database Changes
- [ ] New migration added
- [ ] Migration is reversible (`down()` tested)
- [ ] No database changes

## Testing
- [ ] Unit tests added / updated
- [ ] Integration tests added / updated
- [ ] Manual API testing completed (Postman / curl)
- [ ] Rollback plan verified (for migrations)

## Security
- [ ] New endpoints have auth guards
- [ ] Input DTOs have validation
- [ ] No secrets committed
- [ ] OWASP checklist reviewed

## Checklist
- [ ] Code follows SOP standards
- [ ] Self-review completed
- [ ] OpenAPI docs updated
- [ ] `.env.example` updated if new env vars added
- [ ] No breaking changes to consumers (or versioned)
```

## Code Review Checklist

- [ ] Naming follows conventions from [Coding Standards](./coding-standards.md)?
- [ ] Service layer contains business logic (not controller or repository)?
- [ ] All DTOs validated (class-validator / Bean Validation / FluentValidation)?
- [ ] Unit tests present with ≥ 80% coverage on new code?
- [ ] No `any` types (TypeScript) / raw `Object` params?
- [ ] Errors use domain exceptions with proper HTTP codes?
- [ ] OpenAPI annotations complete on new endpoints?
- [ ] No hardcoded credentials or secrets?
- [ ] Migration has `down()` and was tested?
- [ ] Auth guard + role check on protected endpoints?
- [ ] Commit messages follow [Commit Convention](./commit-convention.md)?
