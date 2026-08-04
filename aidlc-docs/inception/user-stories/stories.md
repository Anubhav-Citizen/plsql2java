# User Stories
## plsql2java — Oracle PL/SQL Legacy Modernization Platform

**Story Format**: Epic-Based with detailed granularity
**Acceptance Criteria**: Given/When/Then (happy path) + Checklist (edge cases & NFRs)
**Delivery Mode**: Stories cover capabilities regardless of CLI or Web UI delivery

---

## Epic 1: Oracle Schema Discovery

*As a user of the plsql2java platform, I need to connect to Oracle databases and import schema definitions so that I can begin the modernization process.*

---

### Story 1.1 — JDBC Live Database Connection

**As a** Modernization Consultant,
**I want to** connect to a live Oracle database using JDBC credentials,
**so that** I can discover all PL/SQL objects directly from the database without needing export files.

**Acceptance Criteria — Happy Path:**
- Given I provide a valid JDBC URL, username, and password
- When I initiate schema discovery
- Then the tool connects to the Oracle database and lists all discoverable objects (packages, procedures, functions, triggers, views, sequences, types)

**Acceptance Criteria — Edge Cases & NFRs:**
- [ ] Connection failure returns a clear error message with retry guidance (not a stack trace)
- [ ] Invalid credentials return an authentication error with actionable message
- [ ] JDBC credentials are never stored in plain text (environment variable or config file with masked display)
- [ ] Connection timeout is configurable (default: 30 seconds)
- [ ] Tool handles Oracle schemas with 0 objects gracefully (reports empty schema)

---

### Story 1.2 — DDL File Import

**As a** Database Engineer,
**I want to** import Oracle DDL/SQL export files for offline schema discovery,
**so that** I can analyze schemas without requiring a live database connection.

**Acceptance Criteria — Happy Path:**
- Given I provide one or more valid Oracle DDL/SQL export files
- When I initiate schema discovery
- Then the tool parses the files and discovers all PL/SQL objects defined within them

**Acceptance Criteria — Edge Cases & NFRs:**
- [ ] Malformed DDL files are reported with the specific file name and line number of the parse error
- [ ] Partially valid files are processed — valid objects are discovered, invalid sections are flagged
- [ ] Uploaded files are validated and sandboxed (no arbitrary code execution from file content)
- [ ] Supported file encodings: UTF-8, ISO-8859-1
- [ ] Maximum supported file size is documented and enforced with a clear error if exceeded

---

### Story 1.3 — Object Type Discovery

**As a** Database Engineer,
**I want to** see all discovered Oracle object types categorized by type,
**so that** I can validate that the tool has found all relevant objects in the schema.

**Acceptance Criteria — Happy Path:**
- Given a schema has been successfully connected or imported
- When discovery completes
- Then the tool reports counts and names of all discovered: packages (spec + body), standalone procedures, standalone functions, triggers, views, sequences, and user-defined types

**Acceptance Criteria — Edge Cases & NFRs:**
- [ ] Discovery coverage is ≥95% of all objects present in the schema
- [ ] Objects with compilation errors in the source schema are flagged (not silently skipped)
- [ ] Package spec and body are linked and reported as a single package unit
- [ ] Discovery results are persisted so they can be referenced in subsequent generate/report commands

---

### Story 1.4 — Discovery Progress Reporting

**As a** Migration Team Member,
**I want to** see real-time progress during schema discovery,
**so that** I know the tool is working and can estimate completion time.

**Acceptance Criteria — Happy Path:**
- Given I have initiated schema discovery on a schema with 100+ objects
- When discovery is running
- Then I see incremental progress updates (objects discovered so far, percentage complete)

**Acceptance Criteria — Edge Cases & NFRs:**
- [ ] Progress is shown in CLI via stdout and in Web UI via a progress indicator
- [ ] Discovery for a 500-object schema completes within 5 minutes
- [ ] If discovery is interrupted, a partial results file is saved with objects discovered so far

---

## Epic 2: Dependency Analysis

*As a user, I need to understand the relationships between Oracle objects so that I can plan the migration order and identify risks.*

---

### Story 2.1 — Dependency Graph Generation

**As a** Technical Lead,
**I want to** generate a full dependency graph of all discovered Oracle objects,
**so that** I can understand which objects depend on which and plan the migration sequence.

**Acceptance Criteria — Happy Path:**
- Given schema discovery has completed
- When I request dependency analysis
- Then the tool produces a dependency graph showing all inter-object relationships (e.g., Package A calls Procedure B, Trigger C references View D)

**Acceptance Criteria — Edge Cases & NFRs:**
- [ ] Circular dependencies are detected and explicitly reported
- [ ] Dependency graph is included in the migration report
- [ ] Objects with no dependencies are identified as migration-ready leaf nodes
- [ ] Dependency analysis completes within the overall 5-minute discovery window

---

### Story 2.2 — Migration Order Recommendation

**As a** Modernization Consultant,
**I want to** receive a recommended migration order based on the dependency graph,
**so that** I can migrate objects in the correct sequence without breaking dependencies.

**Acceptance Criteria — Happy Path:**
- Given dependency analysis has completed
- When I view the migration plan
- Then the tool presents objects in leaf-first migration order (objects with no dependencies first)

**Acceptance Criteria — Edge Cases & NFRs:**
- [ ] Circular dependencies are flagged with a warning and excluded from the automatic ordering
- [ ] Migration order is included in the migration report
- [ ] Objects involved in circular dependencies are marked as requiring manual resolution

---

## Epic 3: PL/SQL Translation Engine

*As a user, I need the tool to translate PL/SQL constructs to Java equivalents so that business logic is preserved in the generated code.*

---

### Story 3.1 — Control Flow Translation

**As a** Java Developer,
**I want** PL/SQL control flow constructs translated to idiomatic Java,
**so that** the generated code is readable and maintainable without manual rewriting.

**Acceptance Criteria — Happy Path:**
- Given a PL/SQL procedure containing IF/ELSIF/ELSE, CASE, LOOP, WHILE, and FOR constructs
- When code generation runs
- Then each construct is translated to its Java equivalent (if/else, switch, while, for)

**Acceptance Criteria — Edge Cases & NFRs:**
- [ ] Nested control flow (IF inside LOOP) is correctly translated
- [ ] GOTO statements are flagged as unsupported and marked for manual review
- [ ] Translation coverage for supported control flow constructs is ≥90%

---

### Story 3.2 — Exception Handling Translation

**As a** Java Developer,
**I want** PL/SQL EXCEPTION WHEN blocks translated to Java try/catch,
**so that** error handling logic is preserved in the generated code.

**Acceptance Criteria — Happy Path:**
- Given a PL/SQL block with EXCEPTION WHEN clauses (including WHEN OTHERS)
- When code generation runs
- Then each exception handler is translated to a corresponding Java catch block

**Acceptance Criteria — Edge Cases & NFRs:**
- [ ] WHEN OTHERS maps to a catch(Exception e) block with a logged warning
- [ ] Named Oracle exceptions (e.g., NO_DATA_FOUND, TOO_MANY_ROWS) map to documented Java equivalents
- [ ] Exception handlers that call RAISE or RAISE_APPLICATION_ERROR are translated with a comment noting the original behavior

---

### Story 3.3 — Cursor and Query Translation

**As a** Java Developer,
**I want** PL/SQL cursor operations translated to Spring Data JPA queries or native queries,
**so that** data access logic is correctly represented in the generated repositories.

**Acceptance Criteria — Happy Path:**
- Given a PL/SQL procedure using explicit cursors or implicit cursor FOR loops
- When code generation runs
- Then cursor operations are translated to Spring Data JPA repository methods or @Query native queries

**Acceptance Criteria — Edge Cases & NFRs:**
- [ ] Simple single-row cursors map to repository findById or findBy methods
- [ ] Multi-row cursor loops map to repository findAll or custom @Query methods returning List<Entity>
- [ ] REF CURSOR parameters are flagged for manual review with a recommendation

---

### Story 3.4 — Bulk Operation Translation

**As a** Java Developer,
**I want** BULK COLLECT and FORALL constructs translated to Java batch operations,
**so that** bulk data processing logic is preserved with equivalent performance characteristics.

**Acceptance Criteria — Happy Path:**
- Given a PL/SQL procedure using BULK COLLECT INTO and FORALL
- When code generation runs
- Then BULK COLLECT maps to a Java List-based fetch and FORALL maps to JPA batch saveAll/deleteAll

**Acceptance Criteria — Edge Cases & NFRs:**
- [ ] BULK COLLECT with LIMIT clause maps to paginated batch fetching
- [ ] FORALL with SAVE EXCEPTIONS is flagged for manual review
- [ ] Generated batch operations include a comment referencing the original PL/SQL construct

---

### Story 3.5 — Built-in Function Translation

**As a** Java Developer,
**I want** Oracle built-in functions (string, date, numeric) translated to Java/Spring equivalents,
**so that** I don't need to manually replace Oracle-specific function calls.

**Acceptance Criteria — Happy Path:**
- Given PL/SQL code using common Oracle built-ins (SUBSTR, INSTR, TO_DATE, TO_CHAR, NVL, DECODE, SYSDATE, etc.)
- When code generation runs
- Then each built-in is translated to its Java/Spring equivalent (String methods, LocalDate, Optional, etc.)

**Acceptance Criteria — Edge Cases & NFRs:**
- [ ] Unsupported or obscure built-ins are flagged with a comment and a TODO marker
- [ ] DBMS_OUTPUT.PUT_LINE is translated to a SLF4J log.debug() call
- [ ] Translation mapping table is documented in the migration report

---

### Story 3.6 — Unsupported Construct Flagging

**As a** Database Engineer,
**I want** Oracle-specific constructs with no Java equivalent to be clearly flagged,
**so that** I know exactly what requires manual migration effort.

**Acceptance Criteria — Happy Path:**
- Given a PL/SQL object containing constructs the tool cannot translate (e.g., DBMS_SCHEDULER, UTL_FILE, advanced DBMS_* packages)
- When code generation runs
- Then each unsupported construct is flagged with a TODO comment in the generated code and listed in the migration report

**Acceptance Criteria — Edge Cases & NFRs:**
- [ ] Flagged constructs include the original PL/SQL line number and construct name
- [ ] Flagged constructs reduce the confidence score of the affected object/method
- [ ] The migration report lists all flagged constructs with migration recommendations
- [ ] Manual rework for supported constructs is <5%

---

## Epic 4: Java Spring Boot Code Generation

*As a user, I need the tool to generate complete, compilable Java Spring Boot artifacts so that I receive production-ready code.*

---

### Story 4.1 — JPA Entity Generation

**As a** Java Developer,
**I want** JPA entity classes generated for Oracle tables and views referenced by PL/SQL objects,
**so that** I have a complete data model in the generated project.

**Acceptance Criteria — Happy Path:**
- Given Oracle tables/views are referenced in the discovered PL/SQL objects
- When code generation runs
- Then a @Entity class is generated for each referenced table/view with correct field mappings

**Acceptance Criteria — Edge Cases & NFRs:**
- [ ] Column types are mapped to appropriate Java types (VARCHAR2→String, NUMBER→Long/BigDecimal, DATE→LocalDate)
- [ ] Primary keys are annotated with @Id and @GeneratedValue where applicable
- [ ] Generated entities compile without errors
- [ ] Entities follow Java naming conventions (camelCase fields, PascalCase class names)

---

### Story 4.2 — Spring Data JPA Repository Generation

**As a** Java Developer,
**I want** Spring Data JPA repository interfaces generated for each entity,
**so that** standard CRUD and query operations are available without manual coding.

**Acceptance Criteria — Happy Path:**
- Given JPA entities have been generated
- When code generation runs
- Then a JpaRepository interface is generated for each entity with standard CRUD methods

**Acceptance Criteria — Edge Cases & NFRs:**
- [ ] Custom query methods are generated for PL/SQL SELECT patterns that map to JPQL
- [ ] Native @Query methods are generated for complex PL/SQL queries that cannot be expressed in JPQL
- [ ] Repository interfaces compile without errors

---

### Story 4.3 — Service Class Generation

**As a** Java Developer,
**I want** @Service classes generated that encapsulate the business logic from PL/SQL packages and procedures,
**so that** the migrated business logic is organized in the correct Spring layer.

**Acceptance Criteria — Happy Path:**
- Given a PL/SQL package with multiple procedures/functions
- When code generation runs
- Then a @Service class is generated with one method per procedure/function, containing the translated business logic

**Acceptance Criteria — Edge Cases & NFRs:**
- [ ] Each service method has a Javadoc comment referencing the original PL/SQL object and procedure name
- [ ] Service classes use constructor injection (not field injection) for dependencies
- [ ] Transactional boundaries are applied (@Transactional) where the original PL/SQL used implicit transactions
- [ ] Generated service classes compile without errors

---

### Story 4.4 — REST Controller Generation

**As a** Java Developer,
**I want** @RestController classes generated that expose migrated services as REST endpoints,
**so that** the migrated business logic is accessible via HTTP.

**Acceptance Criteria — Happy Path:**
- Given service classes have been generated
- When code generation runs
- Then a @RestController is generated for each service with mapped REST endpoints

**Acceptance Criteria — Edge Cases & NFRs:**
- [ ] Endpoints follow RESTful conventions (resource-based URLs, correct HTTP methods)
- [ ] Request/response DTOs are generated with proper validation annotations (@NotNull, @Size, etc.)
- [ ] Controllers return appropriate HTTP status codes (200, 201, 400, 404, 500)
- [ ] Generated controllers compile without errors

---

### Story 4.5 — OpenAPI Specification Generation

**As a** Enterprise Architect,
**I want** OpenAPI 3 specifications generated for all REST endpoints,
**so that** I can review the API surface of the migrated services and share it with stakeholders.

**Acceptance Criteria — Happy Path:**
- Given REST controllers have been generated
- When code generation runs
- Then Springdoc OpenAPI annotations are added to all controllers and DTOs, and an openapi.yaml file is generated

**Acceptance Criteria — Edge Cases & NFRs:**
- [ ] openapi.yaml is valid and parseable by standard OpenAPI tooling
- [ ] All endpoints, request parameters, and response schemas are documented
- [ ] API descriptions reference the original PL/SQL package/procedure names for traceability

---

### Story 4.6 — Unit Test Generation

**As a** Java Developer,
**I want** unit tests generated for all service and repository classes,
**so that** I can validate the migrated business logic without writing tests from scratch.

**Acceptance Criteria — Happy Path:**
- Given service and repository classes have been generated
- When code generation runs
- Then JUnit 5 + Mockito + AssertJ + Spring Boot Test test classes are generated for each service and repository

**Acceptance Criteria — Edge Cases & NFRs:**
- [ ] Each service method has at least one happy-path test
- [ ] Tests for methods with conditional logic include at least one test per branch
- [ ] Repository tests use @DataJpaTest with an in-memory database
- [ ] Generated tests compile and pass for correctly translated logic
- [ ] Test coverage of generated code is ≥80%

---

### Story 4.7 — Complete Maven Project Generation

**As a** Migration Team Member,
**I want** a complete Maven project generated (not just source files),
**so that** I can build and run the migrated application immediately.

**Acceptance Criteria — Happy Path:**
- Given code generation has completed
- When I request the project output
- Then a complete Maven project is produced with pom.xml, standard directory layout, application.yml, Dockerfile, and docker-compose.yml

**Acceptance Criteria — Edge Cases & NFRs:**
- [ ] pom.xml includes all required Spring Boot 3.x dependencies with pinned versions
- [ ] Generated project builds successfully with `mvn clean package`
- [ ] Dockerfile produces a runnable container image
- [ ] docker-compose.yml starts the application and a compatible database for local development
- [ ] application.yml uses environment variable placeholders for all sensitive configuration (no hardcoded credentials)

---

## Epic 5: Confidence Scoring

*As a user, I need confidence scores for migrated objects so that I can prioritize manual review effort.*

---

### Story 5.1 — Object-Level Confidence Score

**As a** Technical Lead,
**I want** a confidence score (0–100%) assigned to every migrated Oracle object,
**so that** I can quickly identify which objects need manual review.

**Acceptance Criteria — Happy Path:**
- Given code generation has completed for a schema
- When I view the migration results
- Then each migrated object (package, procedure, function, trigger, view) has a confidence score

**Acceptance Criteria — Edge Cases & NFRs:**
- [ ] Confidence score reflects: % of constructs successfully translated, complexity, presence of unsupported constructs
- [ ] Objects with all constructs successfully translated score ≥90%
- [ ] Objects with unsupported constructs score proportionally lower
- [ ] Overall migration confidence score is ≥90% for schemas using supported constructs

---

### Story 5.2 — Method-Level Confidence Score

**As a** Java Developer,
**I want** a confidence score assigned to every generated method,
**so that** I can focus my code review on the methods most likely to need correction.

**Acceptance Criteria — Happy Path:**
- Given code generation has completed
- When I view the migration results
- Then each generated service method has an individual confidence score

**Acceptance Criteria — Edge Cases & NFRs:**
- [ ] Method-level scores are visible in the migration report
- [ ] Methods with confidence below the configurable threshold (default: 70%) are highlighted
- [ ] Low-confidence methods include a comment in the generated code indicating the score and reason

---

### Story 5.3 — Confidence Threshold Alerting

**As a** Technical Lead,
**I want** objects and methods below a configurable confidence threshold to be prominently flagged,
**so that** my team knows exactly where to focus manual review effort.

**Acceptance Criteria — Happy Path:**
- Given a confidence threshold has been configured (default: 70%)
- When migration completes
- Then all objects and methods below the threshold are listed in a dedicated "Requires Review" section of the report

**Acceptance Criteria — Edge Cases & NFRs:**
- [ ] Threshold is configurable via CLI flag and Web UI setting
- [ ] Flagged items include the confidence score, object name, and reason for low confidence
- [ ] Summary count of flagged items is shown in the executive summary

---

## Epic 6: Migration Reporting

*As a user, I need comprehensive migration reports so that I can communicate results to stakeholders and guide manual review.*

---

### Story 6.1 — Executive Summary Report

**As an** Enterprise Architect,
**I want** an executive summary in the migration report,
**so that** I can present migration scope, coverage, and risk to non-technical stakeholders.

**Acceptance Criteria — Happy Path:**
- Given migration has completed
- When I generate the report
- Then the report opens with an executive summary showing: total objects discovered, total migrated, total flagged, total skipped, overall confidence score

**Acceptance Criteria — Edge Cases & NFRs:**
- [ ] Executive summary is the first section of both Markdown and HTML reports
- [ ] Summary uses plain language accessible to non-technical readers
- [ ] Summary includes the schema name, migration date, and tool version

---

### Story 6.2 — Traceability Matrix

**As a** Modernization Consultant,
**I want** a traceability matrix in the migration report mapping each PL/SQL object to its generated Java class/method,
**so that** I can verify complete coverage and provide evidence of traceability to clients.

**Acceptance Criteria — Happy Path:**
- Given migration has completed
- When I view the traceability section of the report
- Then each PL/SQL object is mapped to its corresponding generated Java class and method(s)

**Acceptance Criteria — Edge Cases & NFRs:**
- [ ] Traceability coverage is ≥95% of all discovered objects
- [ ] Objects that were skipped or failed are included in the matrix with a status of "Skipped" or "Failed"
- [ ] Traceability matrix is exportable as part of both Markdown and HTML reports

---

### Story 6.3 — Flagged Constructs Report

**As a** Database Engineer,
**I want** a detailed list of all flagged Oracle constructs in the migration report,
**so that** I can provide guidance to the Java team on how to handle each unsupported pattern.

**Acceptance Criteria — Happy Path:**
- Given migration has completed and some constructs were flagged
- When I view the flagged constructs section
- Then each flagged construct is listed with: object name, construct type, original PL/SQL line reference, and a migration recommendation

**Acceptance Criteria — Edge Cases & NFRs:**
- [ ] Recommendations are actionable (e.g., "Replace DBMS_SCHEDULER with Spring @Scheduled")
- [ ] Flagged constructs are grouped by construct type for easy scanning
- [ ] If no constructs are flagged, the section states "No unsupported constructs found"

---

### Story 6.4 — Dual-Format Report Output

**As a** Migration Team Member,
**I want** the migration report available in both Markdown and HTML formats,
**so that** I can share it in different contexts (GitHub, email, browser).

**Acceptance Criteria — Happy Path:**
- Given migration has completed
- When I generate the report
- Then both a .md and an .html version of the report are produced in the output directory

**Acceptance Criteria — Edge Cases & NFRs:**
- [ ] HTML report is self-contained (no external CSS/JS dependencies)
- [ ] Both formats contain identical content
- [ ] HTML report is readable in all modern browsers without additional tooling

---

## Epic 7: CLI Delivery

*As a user, I need a command-line interface so that I can run migrations in automated pipelines and scripted workflows.*

---

### Story 7.1 — Analyze Command

**As a** Modernization Consultant,
**I want** an `analyze` CLI command that runs schema discovery and dependency analysis,
**so that** I can assess a schema before committing to full code generation.

**Acceptance Criteria — Happy Path:**
- Given I run `plsql2java analyze --config migration.yml`
- When the command completes
- Then a discovery and dependency report is written to the output directory

**Acceptance Criteria — Edge Cases & NFRs:**
- [ ] Command accepts both JDBC config and DDL file path as input
- [ ] Command exits with code 0 on success, non-zero on failure
- [ ] Progress is printed to stdout with structured log output
- [ ] `--help` flag prints usage instructions

---

### Story 7.2 — Generate Command

**As a** Migration Team Member,
**I want** a `generate` CLI command that runs the full migration and outputs a Maven project,
**so that** I can execute a complete migration in a single command.

**Acceptance Criteria — Happy Path:**
- Given I run `plsql2java generate --config migration.yml --output ./output`
- When the command completes
- Then a complete Maven project is written to the output directory

**Acceptance Criteria — Edge Cases & NFRs:**
- [ ] Command accepts all configuration via YAML config file and/or CLI flags
- [ ] Command prints per-object progress to stdout
- [ ] Command exits with code 0 on success, non-zero on partial or full failure
- [ ] Full generation for a 500-object schema completes within 15 minutes

---

### Story 7.3 — Report Command

**As a** Technical Lead,
**I want** a `report` CLI command that generates migration reports from a previous analysis run,
**so that** I can regenerate reports without re-running the full migration.

**Acceptance Criteria — Happy Path:**
- Given a previous analysis run exists in the output directory
- When I run `plsql2java report --input ./output --format both`
- Then Markdown and HTML reports are generated from the existing analysis data

**Acceptance Criteria — Edge Cases & NFRs:**
- [ ] `--format` flag accepts: markdown, html, both
- [ ] Command fails with a clear error if the input directory contains no analysis data
- [ ] Report generation completes in under 60 seconds for a 500-object schema

---

## Epic 8: Web Application Delivery

*As a user, I need a browser-based interface so that I can run migrations without command-line expertise.*

---

### Story 8.1 — DDL File Upload

**As a** Database Engineer,
**I want** to upload Oracle DDL/SQL export files via the Web UI,
**so that** I can start schema discovery without using the command line.

**Acceptance Criteria — Happy Path:**
- Given I am on the Web UI upload page
- When I select and upload one or more DDL files
- Then the files are accepted and schema discovery begins automatically

**Acceptance Criteria — Edge Cases & NFRs:**
- [ ] Uploaded files are validated server-side before processing
- [ ] File size limit is enforced with a clear error message
- [ ] Upload progress is shown for large files
- [ ] Unsupported file types are rejected with a clear error

---

### Story 8.2 — JDBC Connection Configuration via Web UI

**As a** Modernization Consultant,
**I want** to configure a JDBC connection to a live Oracle database via the Web UI,
**so that** I can run schema discovery without preparing a config file.

**Acceptance Criteria — Happy Path:**
- Given I am on the Web UI connection configuration page
- When I enter JDBC URL, username, and password and click "Connect"
- Then the tool tests the connection and proceeds to schema discovery on success

**Acceptance Criteria — Edge Cases & NFRs:**
- [ ] Password field is masked in the UI
- [ ] JDBC credentials are not logged or stored beyond the session
- [ ] Connection test result is shown before proceeding to discovery
- [ ] Failed connection shows a specific error (wrong credentials vs. unreachable host)

---

### Story 8.3 — Discovery and Analysis Results View

**As a** Technical Lead,
**I want** to view discovered objects, dependency graph, and confidence scores in the Web UI,
**so that** I can review migration scope before triggering code generation.

**Acceptance Criteria — Happy Path:**
- Given schema discovery has completed
- When I navigate to the results view
- Then I see a list of all discovered objects grouped by type, with their dependency relationships and confidence scores

**Acceptance Criteria — Edge Cases & NFRs:**
- [ ] Results are paginated for schemas with 100+ objects
- [ ] Objects can be filtered by type, confidence score, and flagged status
- [ ] Dependency graph is visualized (tree or graph view)

---

### Story 8.4 — Code Generation Trigger via Web UI

**As a** Migration Team Member,
**I want** to trigger code generation from the Web UI,
**so that** I can generate the Java project without using the command line.

**Acceptance Criteria — Happy Path:**
- Given discovery has completed and I am viewing the results
- When I click "Generate Java Project"
- Then code generation runs and I see real-time progress in the UI

**Acceptance Criteria — Edge Cases & NFRs:**
- [ ] Progress indicator shows per-object generation status
- [ ] Generation can be cancelled mid-run
- [ ] If generation fails for some objects, partial results are still available for download

---

### Story 8.5 — Generated Project Download

**As a** Migration Team Member,
**I want** to download the generated Maven project as a ZIP archive from the Web UI,
**so that** I can deliver the output to the Java development team.

**Acceptance Criteria — Happy Path:**
- Given code generation has completed
- When I click "Download Project"
- Then a ZIP archive containing the complete Maven project is downloaded to my browser

**Acceptance Criteria — Edge Cases & NFRs:**
- [ ] ZIP contains: pom.xml, src/ directory, Dockerfile, docker-compose.yml, and migration reports
- [ ] Download link is available for a configurable retention period
- [ ] ZIP file name includes the schema name and generation timestamp

---

### Story 8.6 — Migration Report View and Download via Web UI

**As an** Enterprise Architect,
**I want** to view and download migration reports directly from the Web UI,
**so that** I can review and share results without accessing the file system.

**Acceptance Criteria — Happy Path:**
- Given migration has completed
- When I navigate to the Reports section
- Then I can view the HTML report inline and download both Markdown and HTML versions

**Acceptance Criteria — Edge Cases & NFRs:**
- [ ] HTML report renders correctly in the Web UI without external dependencies
- [ ] Download buttons are available for both .md and .html formats
- [ ] Reports are associated with the specific migration run and labeled with schema name and date
