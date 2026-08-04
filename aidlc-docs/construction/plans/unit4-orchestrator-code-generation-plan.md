# Unit 4 — Code Generation Plan
## plsql2java-orchestrator: Migration Orchestrator

**Workspace Root**: `c:\project\repo\plsql2java`
**Maven Module**: `plsql2java-orchestrator/`
**Package Root**: `com.plsql2java.orchestration`
**Stories Implemented**: All (technical enabler — enables all 28 stories)
**Dependencies**: `plsql2java-discovery`, `plsql2java-translation`, `plsql2java-codegen`

---

## Unit Context

| Item | Value |
|---|---|
| Unit | Unit 4: Migration Orchestrator |
| Module | plsql2java-orchestrator |
| Depends on | Units 1, 2, 3 |
| Consumed by | plsql2java-cli (Unit 5), plsql2java-web (Unit 6) |
| Key deliverables | MigrationOrchestratorService, ProgressEventBus, domain models, tests |

---

## Step 1: Maven Module Setup
- [x] Create `plsql2java-orchestrator/` directory structure
- [x] Create `plsql2java-orchestrator/pom.xml` with discovery, translation, codegen deps
- **Stories**: infrastructure for all units

## Step 2: Domain Model Classes
- [x] Create `com.plsql2java.orchestration.model.MigrationJobStatus`
- [x] Create `com.plsql2java.orchestration.model.OperationMode`
- [x] Create `com.plsql2java.orchestration.model.PipelineStage`
- [x] Create `com.plsql2java.orchestration.model.MigrationJob`
- [x] Create `com.plsql2java.orchestration.model.AnalysisResult`
- [x] Create `com.plsql2java.orchestration.model.MigrationResult`
- [x] Create `com.plsql2java.orchestration.model.MigrationProgress`
- **Stories**: infrastructure

## Step 3: ProgressEventBus
- [x] Create `com.plsql2java.orchestration.event.ProgressEventBus`
- **Stories**: infrastructure (enables progress reporting for all stories)

## Step 4: MigrationOrchestratorService
- [x] Create `com.plsql2java.orchestration.MigrationOrchestratorService`
- **Stories**: all (technical enabler)

## Step 5: Spring Auto-Configuration
- [x] Create `com.plsql2java.orchestration.OrchestratorAutoConfiguration`
- [x] Create `src/main/resources/META-INF/spring/com.plsql2java.orchestration.OrchestratorAutoConfiguration.imports`
- **Stories**: infrastructure

## Step 6: Unit Tests — Domain Models
- [x] Create `MigrationResultTest`
- [x] Create `MigrationProgressTest`
- **Stories**: foundation

## Step 7: Unit Tests — ProgressEventBus
- [x] Create `ProgressEventBusTest`
- **Stories**: infrastructure

## Step 8: Unit Tests — MigrationOrchestratorService
- [x] Create `MigrationOrchestratorServiceTest`
- **Stories**: all

## Step 9: Integration Test — Full Pipeline
- [x] Create `MigrationOrchestratorIntegrationTest`
- **Stories**: all

## Step 10: Code Summary Documentation
- [x] Create `aidlc-docs/construction/unit4-orchestrator/code/code-summary.md`
- [x] Update `aidlc-docs/aidlc-state.md`
- [x] Append to `aidlc-docs/audit.md`

---

## Story Traceability

| Story Coverage | Steps |
|---|---|
| All 28 stories (technical enabler) | 1–9 |
| Progress reporting (Stories 1.4, 7.1–7.3, 8.3–8.4) | 3, 4, 7, 8 |
| Analyze mode (Stories 1.1–2.2, 7.1) | 4, 8, 9 |
| Generate mode (Stories 3.1–6.4, 7.2) | 4, 8, 9 |
| Report mode (Stories 6.1–6.4, 7.3) | 4, 8, 9 |
