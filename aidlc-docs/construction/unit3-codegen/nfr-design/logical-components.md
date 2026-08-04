# Logical Components — Unit 3: Code Generator + Confidence Scorer + Report Generator

---

## In-Process Components (no external infrastructure)

| Component | Type | Purpose |
|---|---|---|
| JavaCodeGeneratorService | Spring @Service | Generates all Java artifacts from TranslationResults |
| ConfidenceScorerService | Spring @Service | Scores all translated objects and methods |
| MigrationReportGeneratorService | Spring @Service | Assembles and renders Markdown + HTML reports |
| CodegenAutoConfiguration | Spring @Configuration | @ComponentScan for codegen package |

## Classpath Resources (bundled in JAR)

| Resource | Purpose |
|---|---|
| `templates/java/service.ftl` | FreeMarker template for @Service class |
| `templates/java/controller.ftl` | FreeMarker template for @RestController class |
| `templates/java/dto-request.ftl` | FreeMarker template for request DTO |
| `templates/java/dto-response.ftl` | FreeMarker template for response DTO |
| `templates/java/test.ftl` | FreeMarker template for JUnit 5 test class |
| `templates/maven/pom.ftl` | FreeMarker template for pom.xml |
| `templates/maven/application-yml.ftl` | FreeMarker template for application.yml |
| `templates/docker/Dockerfile.ftl` | FreeMarker template for Dockerfile |
| `templates/docker/docker-compose.ftl` | FreeMarker template for docker-compose.yml |

## No External Infrastructure

- No database
- No message queue
- No cache
- No external HTTP calls
- All processing is in-memory; output written to local filesystem
