# Personas
## plsql2java — Oracle PL/SQL Legacy Modernization Platform

---

## Persona 1: Enterprise Architect

**Name**: Alex Chen
**Role**: Enterprise Architect
**Experience**: 15+ years in enterprise software, 5+ years in cloud modernization

### Goals
- Evaluate the full scope and feasibility of a PL/SQL-to-Java migration before committing resources
- Understand the architectural impact of migrating Oracle business logic to microservices
- Ensure the modernized architecture meets enterprise standards and NFRs
- Present migration confidence and risk to executive stakeholders

### Pain Points
- Manual schema analysis takes weeks and is error-prone
- Difficult to quantify migration risk without automated tooling
- Hard to justify modernization investment without concrete metrics
- Existing tools don't provide traceability from PL/SQL to generated Java

### Characteristics
- Focuses on high-level reports, confidence scores, and architecture diagrams
- Needs executive-ready summaries, not raw code
- Evaluates multiple schemas before committing to a migration approach
- Values accuracy metrics and risk indicators

---

## Persona 2: Modernization Consultant

**Name**: Sarah Okafor
**Role**: Modernization Consultant
**Experience**: 8+ years in Oracle-to-Java migrations, expert in PL/SQL and Spring Boot

### Goals
- Run end-to-end migrations efficiently with minimal manual effort
- Customize translation rules for client-specific PL/SQL patterns
- Deliver production-ready Java Spring Boot projects to clients
- Reduce migration timelines from months to weeks

### Pain Points
- Repetitive manual translation of similar PL/SQL patterns across projects
- Inconsistent code quality in manually migrated code
- Difficult to track which PL/SQL objects have been migrated and which remain
- Clients expect full traceability between original PL/SQL and generated Java

### Characteristics
- Power user — uses both CLI and Web UI depending on context
- Runs migrations on multiple client schemas per month
- Needs fine-grained control over translation rules
- Values speed, consistency, and completeness of generated output

---

## Persona 3: Database Engineer

**Name**: Marcus Reyes
**Role**: Database Engineer / DBA
**Experience**: 12+ years in Oracle database administration and PL/SQL development

### Goals
- Validate that the tool correctly discovers all Oracle objects in the schema
- Review flagged constructs and provide guidance on manual migration items
- Ensure business logic embedded in PL/SQL is correctly identified and preserved
- Verify dependency analysis accurately reflects inter-object relationships

### Pain Points
- Java developers don't understand Oracle-specific constructs (BULK COLLECT, FORALL, etc.)
- Business logic buried in complex PL/SQL packages is often missed during migration
- Dependency graphs are hard to produce manually for large schemas
- No standard way to communicate Oracle-specific patterns to Java teams

### Characteristics
- Deep Oracle expertise, limited Java knowledge
- Focuses on discovery accuracy and flagged construct reports
- Reviews migration reports to validate completeness
- Provides input on how Oracle-specific constructs should be handled

---

## Persona 4: Java Developer

**Name**: Priya Sharma
**Role**: Java Developer / Backend Engineer
**Experience**: 6+ years in Java Spring Boot development

### Goals
- Receive clean, compilable, well-structured Java Spring Boot code
- Understand what was generated and why (traceability to original PL/SQL)
- Run generated unit tests and validate business logic correctness
- Minimize manual rework on generated code

### Pain Points
- Generated code from other tools is often unreadable or non-idiomatic
- No tests provided — must write tests from scratch for migrated logic
- Hard to understand what the original PL/SQL was doing without documentation
- Generated code often doesn't follow Spring Boot best practices

### Characteristics
- Consumes the output of the migration tool, doesn't run it
- Focuses on code quality, test coverage, and Spring Boot idioms
- Reviews generated entities, repositories, services, and controllers
- Values readable, maintainable, well-tested code

---

## Persona 5: Technical Lead

**Name**: David Kim
**Role**: Technical Lead / Migration Lead
**Experience**: 10+ years in software engineering, leading migration projects

### Goals
- Oversee migration quality across the entire project
- Track migration progress (objects migrated, flagged, remaining)
- Ensure confidence scores meet the project's quality threshold
- Coordinate between DBAs, Java developers, and architects

### Pain Points
- No single view of migration progress across all schema objects
- Hard to identify which objects need manual attention vs. automated migration
- Difficult to enforce consistent quality standards across a large migration
- Reporting to stakeholders requires manual aggregation of migration data

### Characteristics
- Manages the migration project end-to-end
- Needs dashboards, summary reports, and progress tracking
- Reviews confidence scores and flags items below threshold
- Coordinates handoffs between DBA review and Java developer implementation

---

## Persona 6: Migration Team Member

**Name**: Jordan Taylor
**Role**: Migration Team Member / Junior Developer
**Experience**: 2–4 years, assigned to execute migration tasks

### Goals
- Execute migration tasks as directed by the Technical Lead
- Run the tool against assigned schemas and deliver output artifacts
- Follow the migration report to address flagged items
- Package and deliver generated Maven projects

### Pain Points
- Unclear instructions on how to run the tool and interpret results
- Unsure how to handle flagged constructs without expert guidance
- Difficult to know if the generated output is complete and correct
- No clear workflow for what to do after code generation

### Characteristics
- Executes tasks rather than making architectural decisions
- Needs clear, step-by-step CLI and Web UI workflows
- Relies on migration reports to understand what needs manual attention
- Values clear error messages and actionable guidance
