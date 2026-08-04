# Application Design
## plsql2java — Oracle PL/SQL Legacy Modernization Platform

---

## Architecture Overview

The plsql2java platform is a **layered, component-based Java application** with a clear separation between delivery, orchestration, and core engine layers.

```
+----------------------------------------------------------+
|                   DELIVERY LAYER                         |
|  +-------------------+   +---------------------------+  |
|  |   CLI Component   |   |  Web Application Component|  |
|  | (Picocli/Shell)   |   |  (Spring Boot + Frontend) |  |
|  +-------------------+   +---------------------------+  |
+----------------------------------------------------------+
                          |
+----------------------------------------------------------+
|                  ORCHESTRATION LAYER                     |
|  +----------------------------------------------------+  |
|  |        MigrationOrchestratorService                |  |
|  |  (sequences all core engine calls, manages jobs)   |  |
|  +----------------------------------------------------+  |
+----------------------------------------------------------+
                          |
+----------------------------------------------------------+
|                    CORE ENGINE LAYER                     |
|  +--------------+  +----------------+  +-------------+  |
|  |   Oracle     |  |   Dependency   |  |   PL/SQL    |  |
|  |  Discovery   |  |    Analyzer    |  | Translation |  |
|  |   Service    |  |    Service     |  |   Engine    |  |
|  +--------------+  +----------------+  +-------------+  |
|  +--------------+  +----------------+  +-------------+  |
|  |    Java      |  |   Confidence   |  |  Migration  |  |
|  |    Code      |  |    Scorer      |  |   Report    |  |
|  |  Generator   |  |    Service     |  |  Generator  |  |
|  +--------------+  +----------------+  +-------------+  |
+----------------------------------------------------------+
                          |
+----------------------------------------------------------+
|                   SHARED INFRASTRUCTURE                  |
|  +----------------------------------------------------+  |
|  |  Domain Models | Config | Logging | Progress Events|  |
|  +----------------------------------------------------+  |
+----------------------------------------------------------+
```

---

## Components Summary

| # | Component | Package | Role |
|---|---|---|---|
| 1 | Oracle Discovery Component | `com.plsql2java.discovery` | JDBC + DDL file schema discovery |
| 2 | Dependency Analyzer Component | `com.plsql2java.dependency` | Dependency graph + migration order |
| 3 | PL/SQL Translation Engine | `com.plsql2java.translation` | Rule-based PL/SQL → Java translation |
| 4 | Java Code Generator Component | `com.plsql2java.codegen` | Spring Boot artifact generation |
| 5 | Confidence Scorer Component | `com.plsql2java.scoring` | Object + method confidence scoring |
| 6 | Migration Report Generator | `com.plsql2java.reporting` | Markdown + HTML report generation |
| 7 | Migration Orchestrator Service | `com.plsql2java.orchestration` | End-to-end workflow coordination |
| 8 | CLI Component | `com.plsql2java.cli` | analyze / generate / report commands |
| 9 | Web Application Component | `com.plsql2java.web` | Spring Boot REST API + frontend UI |

---

## Key Design Principles

1. **Delivery layer isolation**: CLI and Web components only call the Orchestrator — never core engine services directly
2. **Orchestrator as single entry point**: All migration workflows flow through MigrationOrchestratorService
3. **Core engine independence**: Core engine components are stateless and independent — no cross-component calls
4. **Extensible translation rules**: New PL/SQL constructs are added by registering a TranslationRule — no engine changes required
5. **Externalized templates**: All Java code generation uses externalized templates (FreeMarker/Mustache)
6. **Progress event bus**: All services emit ProgressEvents — delivery layer subscribes without coupling to business logic
7. **Fail-partial, not fail-all**: Object-level failures are logged and skipped; migration continues for remaining objects

---

## Migration Data Flow

```
Oracle DB / DDL Files
        |
        v
  DiscoveryResult (List<OracleObject>)
        |
        v
  DependencyGraph + ordered migration sequence
        |
        v
  List<TranslationResult> (Java IR + flagged constructs)
        |
        v
  GeneratedProject (Maven project file tree)
        |
        v
  ConfidenceReport (per-object + per-method scores)
        |
        v
  MigrationReport (report.md + report.html)
        |
        v
  Output Directory
```

---

## Operation Modes

| Mode | Trigger | Steps Executed |
|---|---|---|
| Analyze | `plsql2java analyze` / Web "Analyze" | Discovery + Dependency Analysis |
| Generate | `plsql2java generate` / Web "Generate" | Full pipeline (all 6 core engine steps) |
| Report | `plsql2java report` / Web "Download Report" | Load persisted state + Report Generation |

---

## Security Design Highlights

- JDBC credentials sourced from environment variables — never stored in plain text
- DDL file uploads validated and sandboxed before parsing
- Web API authentication enforced on all non-public endpoints (Spring Security)
- HTTP security headers applied on all responses (CSP, HSTS, X-Content-Type-Options, X-Frame-Options, Referrer-Policy)
- All API inputs validated before processing
- No credentials or PII in structured log output

---

## Detailed Artifacts

- **Components**: `aidlc-docs/inception/application-design/components.md`
- **Component Methods**: `aidlc-docs/inception/application-design/component-methods.md`
- **Services**: `aidlc-docs/inception/application-design/services.md`
- **Component Dependencies**: `aidlc-docs/inception/application-design/component-dependency.md`
