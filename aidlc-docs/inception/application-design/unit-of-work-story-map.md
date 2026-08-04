# Unit of Work — Story Map
## plsql2java — Oracle PL/SQL Legacy Modernization Platform

---

## Unit 1: Discovery & Dependency Analysis

| Story | Title | Epic |
|---|---|---|
| 1.1 | JDBC Live Database Connection | Epic 1: Oracle Schema Discovery |
| 1.2 | DDL File Import | Epic 1: Oracle Schema Discovery |
| 1.3 | Object Type Discovery | Epic 1: Oracle Schema Discovery |
| 1.4 | Discovery Progress Reporting | Epic 1: Oracle Schema Discovery |
| 2.1 | Dependency Graph Generation | Epic 2: Dependency Analysis |
| 2.2 | Migration Order Recommendation | Epic 2: Dependency Analysis |

**Story Count**: 6
**Personas Covered**: Modernization Consultant, Database Engineer, Migration Team Member, Technical Lead

---

## Unit 2: PL/SQL Translation Engine

| Story | Title | Epic |
|---|---|---|
| 3.1 | Control Flow Translation | Epic 3: PL/SQL Translation Engine |
| 3.2 | Exception Handling Translation | Epic 3: PL/SQL Translation Engine |
| 3.3 | Cursor and Query Translation | Epic 3: PL/SQL Translation Engine |
| 3.4 | Bulk Operation Translation | Epic 3: PL/SQL Translation Engine |
| 3.5 | Built-in Function Translation | Epic 3: PL/SQL Translation Engine |
| 3.6 | Unsupported Construct Flagging | Epic 3: PL/SQL Translation Engine |

**Story Count**: 6
**Personas Covered**: Java Developer, Database Engineer

---

## Unit 3: Code Generator, Confidence Scorer & Report Generator

| Story | Title | Epic |
|---|---|---|
| 4.1 | JPA Entity Generation | Epic 4: Java Spring Boot Code Generation |
| 4.2 | Spring Data JPA Repository Generation | Epic 4: Java Spring Boot Code Generation |
| 4.3 | Service Class Generation | Epic 4: Java Spring Boot Code Generation |
| 4.4 | REST Controller Generation | Epic 4: Java Spring Boot Code Generation |
| 4.5 | OpenAPI Specification Generation | Epic 4: Java Spring Boot Code Generation |
| 4.6 | Unit Test Generation | Epic 4: Java Spring Boot Code Generation |
| 4.7 | Complete Maven Project Generation | Epic 4: Java Spring Boot Code Generation |
| 5.1 | Object-Level Confidence Score | Epic 5: Confidence Scoring |
| 5.2 | Method-Level Confidence Score | Epic 5: Confidence Scoring |
| 5.3 | Confidence Threshold Alerting | Epic 5: Confidence Scoring |
| 6.1 | Executive Summary Report | Epic 6: Migration Reporting |
| 6.2 | Traceability Matrix | Epic 6: Migration Reporting |
| 6.3 | Flagged Constructs Report | Epic 6: Migration Reporting |
| 6.4 | Dual-Format Report Output | Epic 6: Migration Reporting |

**Story Count**: 14
**Personas Covered**: Java Developer, Enterprise Architect, Technical Lead, Modernization Consultant, Migration Team Member, Database Engineer

---

## Unit 4: Migration Orchestrator

| Story | Title | Epic | Notes |
|---|---|---|---|
| (all) | All stories — orchestrator enables end-to-end flow | All Epics | Orchestrator is infrastructure; no dedicated stories — it enables all stories by wiring Units 1–3 |

**Story Count**: 0 dedicated stories (enables all 28)
**Notes**: The orchestrator is a technical enabler. Its correctness is validated through integration tests and through the CLI/Web stories in Units 5 and 6.

---

## Unit 5: CLI Delivery

| Story | Title | Epic |
|---|---|---|
| 7.1 | Analyze Command | Epic 7: CLI Delivery |
| 7.2 | Generate Command | Epic 7: CLI Delivery |
| 7.3 | Report Command | Epic 7: CLI Delivery |

**Story Count**: 3
**Personas Covered**: Modernization Consultant, Migration Team Member, Technical Lead

---

## Unit 6: Web Application Delivery

| Story | Title | Epic |
|---|---|---|
| 8.1 | DDL File Upload | Epic 8: Web Application Delivery |
| 8.2 | JDBC Connection Configuration via Web UI | Epic 8: Web Application Delivery |
| 8.3 | Discovery and Analysis Results View | Epic 8: Web Application Delivery |
| 8.4 | Code Generation Trigger via Web UI | Epic 8: Web Application Delivery |
| 8.5 | Generated Project Download | Epic 8: Web Application Delivery |
| 8.6 | Migration Report View and Download via Web UI | Epic 8: Web Application Delivery |

**Story Count**: 6 (+ all engine stories enabled via orchestrator)
**Personas Covered**: Database Engineer, Modernization Consultant, Technical Lead, Migration Team Member, Enterprise Architect

---

## Coverage Summary

| Unit | Stories | % of Total |
|---|---|---|
| Unit 1: Discovery & Dependency | 6 | 21% |
| Unit 2: Translation Engine | 6 | 21% |
| Unit 3: Code Generator + Scoring + Reporting | 14 | 50% |
| Unit 4: Migration Orchestrator | 0 (enabler) | — |
| Unit 5: CLI Delivery | 3 | 11% |
| Unit 6: Web Application Delivery | 6 | 21% |
| **Total** | **28** | **100%** |

---

## Persona Coverage by Unit

| Persona | Unit 1 | Unit 2 | Unit 3 | Unit 5 | Unit 6 |
|---|---|---|---|---|---|
| Enterprise Architect | | | X | | X |
| Modernization Consultant | X | | X | X | X |
| Database Engineer | X | X | X | | X |
| Java Developer | | X | X | | |
| Technical Lead | X | | X | X | X |
| Migration Team Member | X | | X | X | X |
