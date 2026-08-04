# Infrastructure Design — Unit 3: Code Generator + Confidence Scorer + Report Generator

## Infrastructure Summary

Unit 3 (`plsql2java-codegen`) is a pure library module. No external infrastructure. Consumed as a Maven dependency by Unit 4 (Orchestrator).

## Compute

- **Runtime**: JVM (Java 17) — in-process within the host application
- **No dedicated compute resource**

## Storage

- **Input**: In-memory (TranslationResults, DiscoveryResult, DependencyGraph passed by reference)
- **Output**: Local filesystem — generated project files written to `outputDir`
- **Templates**: Bundled in JAR as classpath resources

## Security Controls

| Control | Implementation |
|---|---|
| No hardcoded credentials | BR-CG01 — env var placeholders in generated application.yml |
| Non-root Docker user | BR-CG03 — generated Dockerfile creates non-root user |
| Pinned image tags | BR-CG02 — no `latest` in generated Dockerfile/docker-compose |
| Path traversal prevention | NFR-CG-S6 — output path validated before every write |
| Resource cleanup | SECURITY-15 — try-with-resources for all file I/O |
| No credentials in reports | BR-RG07 — report generator strips connection details |

## Build Artifact

- **Output**: `plsql2java-codegen-{version}.jar`
- **Consumed by**: `plsql2java-orchestrator` (Unit 4)
