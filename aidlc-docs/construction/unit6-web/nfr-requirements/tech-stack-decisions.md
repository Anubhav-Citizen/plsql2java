# Tech Stack Decisions — Unit 6: Web Application Delivery

## Spring Boot Starter Web
- **Decision**: `spring-boot-starter-web` (embedded Tomcat)
- **Rationale**: REST API + Thymeleaf serving; consistent with project Spring Boot 3.2.5 baseline

## Spring Boot Starter Thymeleaf
- **Decision**: `spring-boot-starter-thymeleaf`
- **Rationale**: Server-side rendering; no separate frontend build pipeline; simpler than React for this use case

## Spring Security
- **Decision**: `spring-boot-starter-security`
- **Rationale**: SECURITY-08 compliance; deny-by-default; HTTP security headers via `HttpSecurity`

## Spring Boot Starter Actuator (optional)
- **Decision**: Excluded — not needed for this delivery scope
- **Rationale**: No monitoring infrastructure required; reduces attack surface (SECURITY-09)

## Springdoc OpenAPI
- **Decision**: `springdoc-openapi-starter-webmvc-ui:2.5.0`
- **Rationale**: Auto-generates OpenAPI 3 spec from controller annotations; Swagger UI for API exploration

## Jackson
- **Decision**: Inherited from `spring-boot-starter-web`
- **Rationale**: JSON serialization for REST responses; `FAIL_ON_UNKNOWN_PROPERTIES` enabled (SECURITY-13)

## Bootstrap 5
- **Decision**: Bootstrap 5.3.x CSS/JS served as static classpath resources
- **Rationale**: No CDN dependency (avoids SRI complexity); consistent UI; no build pipeline needed

## JUnit 5 + Mockito + MockMvc + AssertJ
- **Decision**: `spring-boot-starter-test` — already in parent BOM
- **Rationale**: MockMvc for controller tests; consistent test stack

## Docker
- **Decision**: `Dockerfile` using `eclipse-temurin:17-jre-alpine` base image (pinned digest)
- **Rationale**: Minimal image size; SECURITY-10 (no `latest` tag); Alpine reduces attack surface
