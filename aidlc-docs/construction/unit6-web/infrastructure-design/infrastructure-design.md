# Infrastructure Design — Unit 6: Web Application Delivery

## Deployment Model: Self-Contained Docker Container

```
Docker Host
└── plsql2java-web container
    ├── JRE 17 (eclipse-temurin:17-jre-alpine, pinned digest)
    ├── plsql2java-web-{version}.jar (fat JAR)
    ├── Port 8080 exposed
    └── Volume: /app/output (generated artifacts)
```

## Runtime Environment

| Requirement | Value |
|---|---|
| Base image | `eclipse-temurin:17-jre-alpine` (pinned digest — SECURITY-10) |
| JVM heap | 512MB min, 1GB max (`-Xms512m -Xmx1g`) |
| Port | 8080 (configurable via `SERVER_PORT`) |
| Temp dir | `/tmp/plsql2java/` (inside container) |
| Output volume | `/app/output` (bind mount for generated artifacts) |
| Network | Oracle JDBC connectivity required for JDBC mode |

## Build Artifact

- **Plugin**: `spring-boot-maven-plugin` repackage
- **Main class**: `com.plsql2java.web.WebApplication`
- **Dockerfile**: multi-stage not required (fat JAR is self-contained)

## Security Hardening (SECURITY-09/10)

- Non-root user in Dockerfile (`USER 1001`)
- No default credentials in image
- `eclipse-temurin` pinned to specific digest (no `latest`)
- Spring Security enabled by default (no unauthenticated access)
- HTTPS termination at reverse proxy (Nginx/load balancer) — HSTS header set by app

## Environment Variables

| Variable | Purpose | Required |
|---|---|---|
| `SERVER_PORT` | HTTP port | No (default: 8080) |
| `SPRING_SECURITY_USER_NAME` | Default admin username | Yes (first run) |
| `SPRING_SECURITY_USER_PASSWORD` | Default admin password | Yes (first run) |
| `PLSQL2JAVA_OUTPUT_DIR` | Output directory path | No (default: /app/output) |
| `PLSQL2JAVA_MAX_FILE_SIZE` | Max upload size | No (default: 50MB) |

## No External Infrastructure Required

- No database (in-memory job registry)
- No message broker
- No cloud services
- Oracle JDBC is the only external runtime dependency (JDBC mode only)
