# Docker Setup — Backend App

> เมื่อถูกขอให้สร้าง Dockerfile หรือ Docker config ให้ generate ไฟล์ต่อไปนี้ทันที และห้ามแสดงแค่ตัวอย่าง — ต้อง output code จริงเสมอ

---

## Prompt: Generate Dockerfile สำหรับ Backend App

```
สร้าง Dockerfile แบบ multi-stage build สำหรับ backend app โดย:

1. Stage 1 — builder
   - ติดตั้ง dependencies และ build app
   - สำหรับ Node TS: compile TypeScript → dist/
   - สำหรับ Spring Boot: build JAR via Maven/Gradle
   - สำหรับ .NET: dotnet publish

2. Stage 2 — runner (production)
   - Node TS: node:20-alpine รัน dist/ เท่านั้น (ไม่มี devDependencies)
   - Spring Boot: eclipse-temurin:21-jre-alpine รัน .jar
   - .NET: mcr.microsoft.com/dotnet/aspnet:8.0-alpine

3. ข้อกำหนด:
   - Health check endpoint (/health หรือ /actuator/health)
   - Non-root user สำหรับ runtime container
   - .dockerignore ที่เหมาะสม
   - docker-compose.yml พร้อม database (PostgreSQL) + app
   - Environment variables ผ่าน .env ไม่ bake ลง image

4. ไฟล์ที่ต้อง generate:
   - Dockerfile
   - .dockerignore
   - docker-compose.yml (dev profile: hot-reload, prod profile: built image)
   - scripts/docker-build.sh
```

---

## ไฟล์ที่ต้อง Generate

---

### NestJS / Node TypeScript

#### `Dockerfile`

```dockerfile
# ─── Stage 1: Builder ────────────────────────────────────────────────────────
FROM node:20-alpine AS builder

RUN corepack enable && corepack prepare pnpm@latest --activate

WORKDIR /app

COPY package.json pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile

COPY . .
RUN pnpm build

# Remove devDependencies for production
RUN pnpm prune --prod

# ─── Stage 2: Runner ─────────────────────────────────────────────────────────
FROM node:20-alpine AS runner

LABEL maintainer="[TEAM_NAME]" version="1.0.0"

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

COPY --from=builder --chown=appuser:appgroup /app/dist ./dist
COPY --from=builder --chown=appuser:appgroup /app/node_modules ./node_modules
COPY --from=builder --chown=appuser:appgroup /app/package.json ./

USER appuser

EXPOSE 3000

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget -qO- http://localhost:3000/health || exit 1

CMD ["node", "dist/main.js"]
```

---

### Spring Boot (Java)

#### `Dockerfile`

```dockerfile
# ─── Stage 1: Builder ────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Layer cache — copy pom first
COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn package -DskipTests -q

# Extract layered JAR for better Docker layer caching
RUN java -Djarmode=layertools -jar target/*.jar extract --destination extracted

# ─── Stage 2: Runner ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runner

LABEL maintainer="[TEAM_NAME]" version="1.0.0"

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy layers in order (least to most frequently changed)
COPY --from=builder --chown=appuser:appgroup /app/extracted/dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /app/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=appuser:appgroup /app/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /app/extracted/application/ ./

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", \
            "org.springframework.boot.loader.launch.JarLauncher"]
```

---

### .NET (C#)

#### `Dockerfile`

```dockerfile
# ─── Stage 1: Builder ────────────────────────────────────────────────────────
FROM mcr.microsoft.com/dotnet/sdk:8.0-alpine AS builder

WORKDIR /app

COPY *.sln .
COPY src/**/*.csproj ./
RUN dotnet restore

COPY . .
RUN dotnet publish src/[ProjectName] -c Release -o /app/publish --no-restore

# ─── Stage 2: Runner ─────────────────────────────────────────────────────────
FROM mcr.microsoft.com/dotnet/aspnet:8.0-alpine AS runner

LABEL maintainer="[TEAM_NAME]" version="1.0.0"

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

COPY --from=builder --chown=appuser:appgroup /app/publish ./

USER appuser

EXPOSE 8080

ENV ASPNETCORE_URLS=http://+:8080
ENV ASPNETCORE_ENVIRONMENT=Production

HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
  CMD wget -qO- http://localhost:8080/health || exit 1

ENTRYPOINT ["dotnet", "[ProjectName].dll"]
```

---

### `.dockerignore` (universal)

```
node_modules
dist
build
target
bin
obj
.git
.gitignore
coverage
*.log
.env
.env.*
!.env.example
.DS_Store
Thumbs.db
**/*.test.ts
**/*.spec.ts
*Test.java
*Tests.cs
docs
README.md
```

---

### `docker-compose.yml`

```yaml
version: "3.9"

services:
  # ── Database ──────────────────────────────────────────────────────────────
  db:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: ${DB_NAME:-app_db}
      POSTGRES_USER: ${DB_USER:-postgres}
      POSTGRES_PASSWORD: ${DB_PASSWORD:-changeme}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USER:-postgres}"]
      interval: 10s
      timeout: 5s
      retries: 5
    profiles: [dev, prod]

  # ── App: Development (hot-reload) ──────────────────────────────────────────
  app-dev:
    image: node:20-alpine                # change to maven / dotnet sdk for other frameworks
    working_dir: /app
    command: sh -c "corepack enable && pnpm install && pnpm start:dev"
    volumes:
      - .:/app
      - /app/node_modules
    ports:
      - "${APP_PORT:-3000}:3000"
    env_file: .env
    environment:
      DB_HOST: db
      NODE_ENV: development
    depends_on:
      db:
        condition: service_healthy
    profiles: [dev]

  # ── App: Production (built image) ─────────────────────────────────────────
  app-prod:
    build:
      context: .
      dockerfile: Dockerfile
      target: runner
    ports:
      - "${APP_PORT:-3000}:3000"
    env_file: .env
    environment:
      DB_HOST: db
      NODE_ENV: production
    depends_on:
      db:
        condition: service_healthy
    restart: unless-stopped
    profiles: [prod]

volumes:
  postgres_data:
```

---

### `scripts/docker-build.sh`

```bash
#!/usr/bin/env bash
set -euo pipefail

IMAGE_NAME="${IMAGE_NAME:-[PROJECT_NAME]-backend}"
TAG="${TAG:-latest}"
REGISTRY="${REGISTRY:-}"

FULL_IMAGE="${REGISTRY:+${REGISTRY}/}${IMAGE_NAME}:${TAG}"

echo "Building: ${FULL_IMAGE}"
docker build --target runner -t "${FULL_IMAGE}" .
echo "Build complete: ${FULL_IMAGE}"

if [[ "${PUSH:-false}" == "true" ]]; then
  echo "Pushing: ${FULL_IMAGE}"
  docker push "${FULL_IMAGE}"
fi
```

---

## Docker Commands

```bash
# Development (hot-reload + DB)
docker compose --profile dev up

# Production build & run
docker compose --profile prod up --build

# Run DB migrations after container starts
docker compose exec app-prod npm run migration:run    # NestJS
docker compose exec app-prod ./mvnw flyway:migrate    # Spring Boot
docker compose exec app-prod dotnet ef database update # .NET

# Build image manually
bash scripts/docker-build.sh

# Build + push to registry
IMAGE_NAME=my-backend TAG=v1.2.0 REGISTRY=ghcr.io/myorg PUSH=true bash scripts/docker-build.sh

# View logs
docker compose logs -f app-dev

# Shell into running container
docker compose exec app-dev sh
```

---

## Best Practices

| Rule | Why |
|------|-----|
| Multi-stage build | Final image has no build tools, devDeps, or source code |
| Non-root user | Reduces blast radius if container is compromised |
| HEALTHCHECK | Orchestrators (K8s, ECS) know when app is ready |
| Layer caching (copy lockfile first) | Faster rebuilds on code-only changes |
| `--frozen-lockfile` / `-DskipTests` | Deterministic CI builds |
| Secrets via env, not ARG | ARGs can be inspected in image history |
| `UseContainerSupport` (JVM) | JVM respects cgroup memory limits in containers |
