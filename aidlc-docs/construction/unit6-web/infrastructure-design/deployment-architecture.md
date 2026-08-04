# Deployment Architecture — Unit 6: Web Application Delivery

## Architecture: Containerized Spring Boot Web Application

```
+----------------------------------------------------------+
|  Docker Host                                             |
|                                                          |
|  +----------------------------------------------------+  |
|  |  plsql2java-web container (:8080)                  |  |
|  |                                                    |  |
|  |  +----------------------------------------------+  |  |
|  |  |  Spring Boot Application                     |  |  |
|  |  |  +------------------+  +-----------------+   |  |  |
|  |  |  | Spring Security  |  | Thymeleaf UI    |   |  |  |
|  |  |  | (deny-by-default)|  | (/ /progress    |   |  |  |
|  |  |  +------------------+  |  /report)        |   |  |  |
|  |  |         |              +-----------------+   |  |  |
|  |  |  +-------v-----------+                       |  |  |
|  |  |  | REST API           |                       |  |  |
|  |  |  | /api/migrations/*  |                       |  |  |
|  |  |  +-------------------+                       |  |  |
|  |  |         |                                     |  |  |
|  |  |  +-------v-----------+  +------------------+  |  |  |
|  |  |  | MigrationJobService|  | SseEmitterRegistry|  |  |  |
|  |  |  | (@Async)           |  | (SSE fan-out)    |  |  |  |
|  |  |  +-------------------+  +------------------+  |  |  |
|  |  |         |                        ^             |  |  |
|  |  |  +-------v-----------+           |             |  |  |
|  |  |  | Orchestrator       +-----------+             |  |  |
|  |  |  | (Units 1-4)        | WebProgressListener    |  |  |
|  |  |  +-------------------+                        |  |  |
|  |  +----------------------------------------------+  |  |
|  |                                                    |  |
|  |  Volume: /app/output  (generated artifacts)        |  |
|  +----------------------------------------------------+  |
|                                                          |
|  Optional: Nginx reverse proxy (HTTPS termination)       |
+----------------------------------------------------------+
```

## Build Pipeline

```
mvn package
    └── spring-boot-maven-plugin:repackage
        └── plsql2java-web-{version}.jar

docker build -t plsql2java-web:{version} .
docker run -p 8080:8080 \
  -e SPRING_SECURITY_USER_NAME=admin \
  -e SPRING_SECURITY_USER_PASSWORD=<password> \
  -v $(pwd)/output:/app/output \
  plsql2java-web:{version}
```

## docker-compose.yml (Development)

```yaml
version: '3.8'
services:
  plsql2java-web:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_SECURITY_USER_NAME=admin
      - SPRING_SECURITY_USER_PASSWORD=${ADMIN_PASSWORD}
      - PLSQL2JAVA_OUTPUT_DIR=/app/output
    volumes:
      - ./output:/app/output
    restart: unless-stopped
```

## Dockerfile

```dockerfile
FROM eclipse-temurin:17-jre-alpine@sha256:<pinned-digest>
RUN addgroup -S plsql2java && adduser -S plsql2java -G plsql2java
WORKDIR /app
COPY target/plsql2java-web-*.jar app.jar
RUN mkdir -p /app/output && chown plsql2java:plsql2java /app/output
USER 1001
EXPOSE 8080
ENTRYPOINT ["java", "-Xms512m", "-Xmx1g", "-jar", "app.jar"]
```
