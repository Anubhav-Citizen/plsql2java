# Requirements Clarification Questions

Please answer each question by filling in the letter choice after the `[Answer]:` tag.
If none of the options match your needs, choose the last option (Other) and describe your preference.
Let me know when you're done.

---

## Section 1: Oracle Connectivity & Discovery

## Question 1
How will the tool connect to the Oracle database for schema discovery?

A) JDBC connection to a live Oracle database (direct connection)
B) Oracle DDL/SQL export files (offline — no live DB connection needed)
C) Both — support live connection AND offline file import
D) Other (please describe after [Answer]: tag below)

[Answer]: C

---

## Question 2
What Oracle database objects should be discovered and migrated?

A) Packages and procedures only
B) Packages, procedures, functions, triggers, views, sequences, and types
C) All of the above plus tables, indexes, and constraints
D) Other (please describe after [Answer]: tag below)

[Answer]: B

---

## Question 3
How should the tool handle Oracle-specific constructs with no direct Java equivalent (e.g., BULK COLLECT, FORALL, CURSOR loops, DBMS_* packages)?

A) Flag them as manual migration items with recommendations
B) Attempt best-effort translation with confidence scoring
C) Both — attempt translation AND flag low-confidence items for manual review
D) Other (please describe after [Answer]: tag below)

[Answer]: C

---

## Section 2: Generated Java Architecture

## Question 4
What Java version should the generated code target?

A) Java 11 (LTS)
B) Java 17 (LTS)
C) Java 21 (LTS)
D) Other (please describe after [Answer]: tag below)

[Answer]: B

---

## Question 5
What Spring Boot version should the generated code target?

A) Spring Boot 2.7.x (Java 11/17 compatible)
B) Spring Boot 3.x (Java 17+ required)
C) Make it configurable — user selects at generation time
D) Other (please describe after [Answer]: tag below)

[Answer]: B

---

## Question 6
What database access strategy should be used in the generated Java code?

A) JPA/Hibernate only (entity-based ORM)
B) Spring Data JPA with repositories
C) Both JPA entities AND native queries where needed (e.g., complex PL/SQL logic)
D) Other (please describe after [Answer]: tag below)

[Answer]: Spring Data JPA with repositories AND native queries where needed (e.g., complex PL/SQL logic)

---

## Question 7
What REST API style should be generated?

A) Standard REST with Spring MVC (@RestController)
B) REST with OpenAPI/Swagger 3 annotations
C) Both REST controllers AND OpenAPI spec generation
D) Other (please describe after [Answer]: tag below)

[Answer]: C

---

## Section 3: Tool Delivery Model

## Question 8
How will this modernization tool be delivered and used?

A) Command-line tool (CLI) — run against a schema, output generated Java project
B) Web application — browser-based UI for upload, analysis, and code download
C) IDE plugin (e.g., IntelliJ, VS Code)
D) Other (please describe after [Answer]: tag below)

[Answer]: A and B both

---

## Question 9
What is the expected output of the tool?

A) Generated Java source files only (ready to compile)
B) Complete Maven/Gradle project (pom.xml/build.gradle + source + tests)
C) Complete project + Docker/deployment configuration
D) Other (please describe after [Answer]: tag below)

[Answer]: C

---

## Section 4: AI/LLM Integration

## Question 10
How should AI/LLM be used in the modernization process?

A) No LLM — rule-based translation only (deterministic, no external API calls)
B) LLM for business rule extraction and complex logic translation (e.g., AWS Bedrock, OpenAI)
C) LLM as optional enhancement — rule-based by default, LLM when configured
D) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Question 11
If LLM integration is included, which provider should be supported?

A) AWS Bedrock only
B) OpenAI only
C) Both AWS Bedrock and OpenAI (configurable)
D) Other (please describe after [Answer]: tag below)

[Answer]: None

---

## Section 5: Confidence Scoring & Reporting

## Question 12
What should the confidence scoring system evaluate?

A) Per-object confidence only (e.g., "Package X: 85% confidence")
B) Per-method/procedure confidence (granular scoring per unit)
C) Both object-level AND method-level confidence scores
D) Other (please describe after [Answer]: tag below)

[Answer]: C

---

## Question 13
What migration report formats should be generated?

A) Markdown report only
B) HTML report only
C) Both Markdown and HTML reports
D) Other (please describe after [Answer]: tag below)

[Answer]: C

---

## Section 6: Non-Functional Requirements

## Question 14
What is the expected scale of Oracle schemas this tool needs to handle?

A) Small schemas — up to 50 PL/SQL objects
B) Medium schemas — up to 500 PL/SQL objects
C) Large enterprise schemas — 500+ PL/SQL objects
D) Other (please describe after [Answer]: tag below)

[Answer]: B

---

## Question 15
What build system should the generated Java projects use?

A) Maven only
B) Gradle only
C) Both — user selects at generation time
D) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Question 16
What unit test framework should be used in generated tests?

A) JUnit 5 + Mockito
B) JUnit 5 + Mockito + AssertJ
C) JUnit 5 + Mockito + AssertJ + Spring Boot Test
D) Other (please describe after [Answer]: tag below)

[Answer]: C

---

## Section 7: Security Extension

## Question 17
Should security extension rules be enforced for this project?

A) Yes — enforce all SECURITY rules as blocking constraints (recommended for production-grade applications)
B) No — skip all SECURITY rules (suitable for PoCs, prototypes, and experimental projects)
C) Other (please describe after [Answer]: tag below)

[Answer]: A

---
