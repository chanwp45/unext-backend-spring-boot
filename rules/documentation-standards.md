# Documentation Standards — Backend

## Required Files per Project

| File | Purpose |
|------|---------|
| `README.md` | Project setup, quick start, architecture overview |
| `CLAUDE.md` | SOP & AI assistant guidelines |
| `.env.example` | All required environment variables with safe placeholder values |
| `docs/` folder | Detailed guides (see below) |

## docs/ Folder Structure

```
docs/
├── architecture-overview.md          # System design, component diagram, data flow
├── api-documentation.md              # API conventions (supplement to Swagger UI)
├── database-schema.md                # ER diagram, table descriptions, index rationale
├── deployment-guide.md               # Deploy steps for each environment
├── local-development.md              # Step-by-step local setup including DB seed
├── architecture-decisions/           # ADR (Architecture Decision Records)
│   ├── 001-use-jwt-for-auth.md
│   ├── 002-use-typeorm-over-prisma.md
│   └── [NNN]-[decision-title].md
└── [custom-doc].md
```

## ADR (Architecture Decision Record) Format

```markdown
# ADR-[NNN]: [Decision Title]

## Status
[Proposed | Accepted | Deprecated | Superseded by ADR-NNN]

## Context
What is the problem or situation that requires a decision?

## Decision
What was decided?

## Consequences
What are the positive and negative outcomes of this decision?

## Alternatives Considered
What other options were evaluated and why were they rejected?
```

## OpenAPI Documentation Rules

Every endpoint must have:

```typescript
// NestJS — minimum required annotations
@ApiTags('resource-name')           // group in Swagger UI
@ApiOperation({ summary: '...' })   // one-line description
@ApiResponse({ status: 200, ... })  // success response with DTO type
@ApiResponse({ status: 4xx, ... })  // all expected error responses
@ApiBearerAuth()                    // if endpoint requires JWT
```

```java
// Spring Boot — springdoc-openapi
@Tag(name = "resource-name")
@Operation(summary = "...")
@ApiResponses({ @ApiResponse(...), @ApiResponse(...) })
@SecurityRequirement(name = "bearerAuth")  // if protected
```

## README.md Required Sections

```markdown
# [Project Name]

## Overview
One paragraph description.

## Tech Stack
| Category | Tool/Version |
|----------|-------------|
| Framework | NestJS 10 / Spring Boot 3 / .NET 8 |
| Language  | TypeScript 5 / Java 21 / C# 12 |
| Database  | PostgreSQL 16 |
| ORM       | TypeORM / JPA / EF Core |

## Prerequisites
- Node.js 20+ / JDK 21+ / .NET 8 SDK
- Docker & Docker Compose
- PostgreSQL 16 (or use Docker)

## Quick Start
[commands]

## Environment Variables
See `.env.example` for all required variables.

## API Documentation
Available at `http://localhost:[PORT]/api-docs` when running locally.

## Running Tests
[commands]

## Database Migrations
[commands]
```

## When to Update CLAUDE.md

- When team agrees on new coding or architecture standards
- When framework or ORM version changes significantly
- When new cross-cutting rules are established (logging, error handling)
- At least **quarterly** for maintenance review
- Always update version number and "Last Updated" date
