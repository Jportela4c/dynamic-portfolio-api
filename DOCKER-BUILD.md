# Docker Build Strategy

This project uses **two Dockerfile approaches** for optimal workflow:

## 1. Fast Local Build (Auto-Selected for Development)

**Files:** `Dockerfile.local`, `ofb-mock-server/Dockerfile.local`

**When it runs:**
- `task run` (detects Maven, builds locally)
- `task rebuild-all` (explicit local build)
- `task rebuild-api` (explicit local build)

**What it does:**
- Compiles Java code **locally** on Mac (faster)
- Docker just copies the pre-built JAR
- Faster iteration during development

**Use case:** Active development, rapid iteration

```bash
# Auto-detects Maven and uses fast local build
task run
task rebuild-all
```

## 2. Multi-Stage Build (Fallback, Foolproof)

**Files:** `Dockerfile`, `ofb-mock-server/Dockerfile`

**When it runs:**
- `task run` (if Maven not available)
- `docker compose build` (direct Docker commands)
- CI/CD pipelines

**What it does:**
- Compiles Java code **inside Docker** using Maven
- Self-contained, works on any machine
- No local Maven/Java required

**Use case:** CI/CD, new developers without Maven, clean builds

```bash
# Multi-stage build (no local Maven required)
docker compose build
docker compose up
```

## Summary

| Approach | Speed | Requirements | Use Case |
|----------|-------|--------------|----------|
| Local (`Dockerfile.local`) | ⚡ Fast | Maven/Java | Development (task run) |
| Multi-stage (`Dockerfile`) | 🐌 Slower | None | CI/CD, first-time setup |

**Smart default:** `task run` auto-detects Maven and uses fast local build. Falls back to multi-stage if Maven unavailable.
