# Requirements Document
## plsql2java — Oracle PL/SQL Legacy Modernization Platform

---

## Intent Analysis Summary

- **User Request**: Build an AI-powered platform to modernize Oracle PL/SQL legacy applications into Java Spring Boot microservices
- **Request Type**: New Project (Greenfield)
- **Scope**: System-wide — full modernization platform with CLI, Web UI, analysis engine, and code generation engine
- **Complexity**: Complex — multi-component platform, multiple user personas, Oracle schema analysis, rule-based translation, REST API generation, confidence scoring, and reporting

---

## Functional Requirements

### FR-01: Oracle Schema Discovery

- **FR-01.1**: Connect to a live Oracle database via JDBC for schema discovery
- **FR-01.2**: Import Oracle DDL/SQL export files for offline schema discovery (no live DB required)
- **FR-01.3**: Discover and analyze the following Oracle object types:
  - Packages (spec + body)
  - Procedures (standalone)
  - Functions (standalone)
  - Triggers
  - Views
  - Sequences
  - User-defined Types (object types, collection types)
- **FR-01.4**: Achieve ≥95% schema discovery coverage across all supported object types
- **FR-01.5**: Extract and preserve inter-object dependencies (e.g., package calls procedure, trigger references table)

### FR-02: Dependency Analysis

- **FR-02.1**: Build a full dependency graph of all discovered Oracle objects
- **FR-02.2**: Identify circular dependencies and report them
- **FR-02.3**: Determine migration order based on dependency graph (leaf-first ordering)
- **FR-02.4**: Map Oracle object dependencies to Java component dependencies

### FR-03: Business Rule Extraction

- **FR-03.1**: Extract business rules from PL/SQL logic using rule-based static analysis (no LLM)
- **FR-03.2**: Identify and document: validation rules, calculation logic, conditional branching, exception handling patterns, and data transformation logic
- **FR-03.3**: Achieve ≥90% business rule extraction accuracy for supported PL/SQL constructs
- **FR-03.4**: Maintain full traceability from extracted business rule → source PL/SQL object → generated Java class/method

### FR-04: PL/SQL Translation Engine (Rule-Based)

- **FR-04.1**: Translate PL/SQL constructs to Java using deterministic rule-based translation (no LLM/AI calls)
- **FR-04.2**: Support translation of:
  - Variable declarations → Java field/local variable declarations
  - Control flow (IF/ELSIF/ELSE, CASE, LOOP, WHILE, FOR) → Java equivalents
  - Exception handling (EXCEPTION WHEN) → Java try/catch blocks
  - Cursor operations → Spring Data JPA queries or native queries
  - BULK COLLECT → Java List-based batch operations
  - FORALL → Java batch insert/update via JPA
  - String/date/numeric built-in functions → Java/Spring equivalents
  - DBMS_OUTPUT → SLF4J logging
- **FR-04.3**: Flag Oracle-specific constructs with no direct Java equivalent for manual review
- **FR-04.4**: Attempt best-effort translation for complex constructs AND assign a low confidence score when translation is uncertain
- **FR-04.5**: Achieve ≥90% automated code generation coverage for supported constructs

### FR-05: Java Spring Boot Code Generation

- **FR-05.1**: Generate Java 17 source code targeting Spring Boot 3.x
- **FR-05.2**: Generate the following Java artifacts per migrated Oracle object:
  - **JPA Entities**: For tables/views referenced by PL/SQL objects
  - **Spring Data JPA Repositories**: For standard CRUD and query operations
  - **Native Query Repositories**: For complex PL/SQL logic that cannot be expressed via JPQL
  - **Service Classes**: Encapsulating business logic translated from PL/SQL packages/procedures/functions
  - **REST Controllers**: Exposing migrated services as REST endpoints using Spring MVC (@RestController)
  - **OpenAPI/Swagger 3 Annotations**: On all REST controllers and DTOs
  - **OpenAPI Spec File**: Generated openapi.yaml/openapi.json per migrated service group
  - **Unit Tests**: JUnit 5 + Mockito + AssertJ + Spring Boot Test for all generated service and repository classes
- **FR-05.3**: Generated REST APIs follow RESTful conventions (resource-based URLs, standard HTTP methods, proper status codes)
- **FR-05.4**: Generated code must compile without errors for supported PL/SQL constructs

### FR-06: Complete Maven Project Generation

- **FR-06.1**: Generate a complete Maven project structure (not just source files):
  - `pom.xml` with all required Spring Boot 3.x dependencies
  - Standard Maven directory layout (`src/main/java`, `src/main/resources`, `src/test/java`)
  - `application.properties` / `application.yml` with configurable datasource and server settings
- **FR-06.2**: Generated `pom.xml` must include: Spring Boot Starter Web, Spring Data JPA, Spring Boot Starter Test, Springdoc OpenAPI, database driver (configurable), Lombok (optional)
- **FR-06.3**: Generate Docker configuration:
  - `Dockerfile` for the generated Spring Boot application
  - `docker-compose.yml` for local development (app + database)

### FR-07: Confidence Scoring

- **FR-07.1**: Assign a confidence score (0–100%) to every migrated Oracle object
- **FR-07.2**: Assign a confidence score (0–100%) to every migrated method/procedure/function
- **FR-07.3**: Confidence score factors:
  - Percentage of constructs successfully translated (vs. flagged for manual review)
  - Complexity of the original PL/SQL logic
  - Presence of unsupported constructs
- **FR-07.4**: Achieve ≥90% migration confidence score for schemas using supported constructs
- **FR-07.5**: Objects/methods below a configurable confidence threshold (default: 70%) are flagged prominently in reports

### FR-08: Migration Reporting

- **FR-08.1**: Generate a migration report in both Markdown and HTML formats
- **FR-08.2**: Report must include:
  - Executive summary (total objects discovered, migrated, flagged, skipped)
  - Per-object confidence scores
  - Per-method confidence scores
  - List of unsupported/flagged constructs with recommendations
  - Traceability matrix (PL/SQL object → Java class/method)
  - Dependency graph summary
- **FR-08.3**: ≥95% traceability coverage in the generated report

### FR-09: CLI Delivery

- **FR-09.1**: Provide a command-line interface (CLI) for the modernization tool
- **FR-09.2**: CLI must support:
  - `analyze` command: Connect to DB or read DDL files, output discovery + dependency report
  - `generate` command: Run full migration and output generated Maven project
  - `report` command: Generate migration report from a previous analysis run
- **FR-09.3**: CLI accepts configuration via a YAML/properties config file and/or command-line flags
- **FR-09.4**: CLI outputs progress to stdout with structured logging

### FR-10: Web Application Delivery

- **FR-10.1**: Provide a browser-based web application for the modernization tool
- **FR-10.2**: Web UI must support:
  - Upload Oracle DDL/SQL export files
  - Configure JDBC connection to a live Oracle database
  - Trigger schema discovery and analysis
  - View discovered objects, dependency graph, and confidence scores
  - Trigger code generation
  - Download generated Maven project as a ZIP archive
  - View and download migration reports (Markdown + HTML)
- **FR-10.3**: Web application built with Spring Boot (backend REST API) + a frontend (technology TBD in design phase)
- **FR-10.4**: Web application is self-contained and runnable via Docker

---

## Non-Functional Requirements

### NFR-01: Performance & Scale

- **NFR-01.1**: Support Oracle schemas with up to 500 PL/SQL objects (medium enterprise scale)
- **NFR-01.2**: Schema discovery and analysis for a 500-object schema must complete within 5 minutes
- **NFR-01.3**: Full code generation for a 500-object schema must complete within 15 minutes
- **NFR-01.4**: Web application must support at least 10 concurrent users without degradation

### NFR-02: Accuracy

- **NFR-02.1**: ≥95% schema discovery coverage
- **NFR-02.2**: ≥90% automated code generation coverage for supported constructs
- **NFR-02.3**: ≥90% business rule extraction accuracy
- **NFR-02.4**: ≥95% traceability coverage
- **NFR-02.5**: ≥90% migration confidence score for supported constructs
- **NFR-02.6**: <5% manual rework for supported PL/SQL constructs

### NFR-03: Reliability

- **NFR-03.1**: Tool must handle malformed or incomplete PL/SQL gracefully (log error, skip object, continue)
- **NFR-03.2**: JDBC connection failures must be handled with clear error messages and retry guidance
- **NFR-03.3**: Generated code must be syntactically valid Java (compilable) for all supported constructs

### NFR-04: Maintainability

- **NFR-04.1**: Translation rules must be externalized and configurable (not hardcoded)
- **NFR-04.2**: Adding support for a new PL/SQL construct must not require changes to the core engine
- **NFR-04.3**: Codebase must have ≥80% unit test coverage

### NFR-05: Security (SECURITY rules enforced — see Extension Configuration)

- **NFR-05.1**: JDBC credentials must never be stored in plain text; use environment variables or a secrets manager
- **NFR-05.2**: Uploaded DDL files must be validated and sandboxed (no arbitrary code execution)
- **NFR-05.3**: Web application REST API must enforce authentication on all non-public endpoints
- **NFR-05.4**: All HTTP endpoints must include required security headers (CSP, HSTS, X-Content-Type-Options, X-Frame-Options, Referrer-Policy)
- **NFR-05.5**: All API inputs must be validated (type, length, format) before processing
- **NFR-05.6**: Structured logging must be used; no credentials or PII in logs
- **NFR-05.7**: All dependencies must use pinned versions with a lock file; vulnerability scanning in CI/CD

### NFR-06: Usability

- **NFR-06.1**: CLI must provide clear, actionable error messages
- **NFR-06.2**: Web UI must display progress indicators during long-running operations
- **NFR-06.3**: Migration reports must be human-readable without requiring technical Oracle/Java expertise

### NFR-07: Portability

- **NFR-07.1**: The tool itself must run on Linux, macOS, and Windows
- **NFR-07.2**: Generated Maven projects must be buildable on any standard Java 17 + Maven environment
- **NFR-07.3**: Docker images must be provided for both the tool and generated applications

---

## Extension Configuration

| Extension | Enabled | Decided At |
|---|---|---|
| Security Baseline (SECURITY-01 through SECURITY-15) | Yes | Requirements Analysis |

---

## Target Users

| Persona | Primary Use |
|---|---|
| Enterprise Architect | Evaluate migration scope, review reports, approve architecture |
| Modernization Consultant | Run full migrations, customize translation rules |
| Database Engineer | Validate Oracle discovery, review flagged constructs |
| Java Developer | Review and refine generated code, run unit tests |
| Technical Lead | Oversee migration quality, review confidence scores |
| Migration Team | Execute migrations, generate reports, deliver artifacts |

---

## Success Metrics

| Metric | Target |
|---|---|
| Schema discovery coverage | ≥95% |
| Automated code generation coverage | ≥90% |
| Business rule extraction accuracy | ≥90% |
| Traceability coverage | ≥95% |
| Migration confidence score | ≥90% |
| Manual rework for supported constructs | <5% |
| Manual migration effort reduction | 70% |
| Migration defect reduction | 60% |

---

## Out of Scope

- LLM/AI-based translation (rule-based only per Q10)
- Oracle table/index/constraint migration (schema DDL migration — FR-01.3 covers discovery only for context; table migration is out of scope)
- Support for non-Oracle databases as source
- Gradle build system for generated projects (Maven only per Q15)
- IDE plugin delivery (CLI + Web only per Q8)
