# Tech Stack Decisions — Unit 3: Code Generator + Confidence Scorer + Report Generator

---

## FreeMarker (Template Engine for Java Artifacts)

- **Decision**: FreeMarker 2.3.33 (`org.freemarker:freemarker`) — already in parent BOM
- **Rationale**: Mature, well-tested Java template engine; templates are externalized in `src/main/resources/templates/`; supports complex conditional logic needed for service/controller generation
- **Templates location**: `src/main/resources/templates/java/`, `templates/maven/`, `templates/docker/`, `templates/report/`

## String Assembly (Report Rendering)

- **Decision**: Pure Java StringBuilder for Markdown and HTML report rendering
- **Rationale**: Reports have a fixed, well-defined structure; no template engine overhead needed; self-contained HTML is simpler to produce with direct string assembly than with a template

## Jackson (Serialization)

- **Decision**: Jackson (`com.fasterxml.jackson.core:jackson-databind`) — already in parent BOM
- **Rationale**: Serialize/deserialize GeneratedProject manifest; consistent with Units 1 and 2

## Spring Context

- **Decision**: `spring-context` (via Spring Boot BOM)
- **Rationale**: `@Service`, `@Component` for all three service classes; consistent with Units 1 and 2

## SLF4J + Logback

- **Decision**: SLF4J API + Logback (via Spring Boot starter)
- **Rationale**: Structured logging; MDC for object-level context; consistent with Units 1 and 2

## JUnit 5 + Mockito + AssertJ

- **Decision**: `spring-boot-starter-test` — already in parent BOM
- **Rationale**: Consistent test stack across all units

## No Additional Dependencies

- No external Markdown-to-HTML converter (HTML rendered directly)
- No additional HTTP client
- No database driver
