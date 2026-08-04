# Unit of Work Dependencies
## plsql2java — Oracle PL/SQL Legacy Modernization Platform

---

## Dependency Matrix

| Unit | Depends On | Dependency Type |
|---|---|---|
| Unit 1: Discovery & Dependency | (none — foundation unit) | — |
| Unit 2: Translation Engine | Unit 1 (OracleObject model) | Compile-time (domain model) |
| Unit 3: Code Generator + Scoring + Reporting | Unit 1 (DiscoveryResult, DependencyGraph), Unit 2 (TranslationResult, JavaIR) | Compile-time (domain models) |
| Unit 4: Migration Orchestrator | Unit 1, Unit 2, Unit 3 (all services) | Compile-time (service interfaces) |
| Unit 5: CLI Delivery | Unit 4 (MigrationOrchestratorService, MigrationConfig) | Compile-time |
| Unit 6: Web Application Delivery | Unit 4 (MigrationOrchestratorService, MigrationConfig) | Compile-time |

---

## Build Sequence

```
Unit 1: Discovery & Dependency
        |
        v
Unit 2: Translation Engine
        |
        v
Unit 3: Code Generator + Scoring + Reporting
        |
        v
Unit 4: Migration Orchestrator
        |
        +------------------+
        v                  v
Unit 5: CLI           Unit 6: Web App
```

- Units 1 → 2 → 3 → 4 must be built sequentially (strict dependency chain)
- Units 5 and 6 both depend only on Unit 4 — they can be developed in parallel after Unit 4 is complete
- Per the agreed approach (Q3=A), Unit 5 (CLI) is developed before Unit 6 (Web App) to validate the engine first

---

## Maven Build Order

Maven multi-module reactor will resolve the build order automatically based on `<dependencies>` in each module's pom.xml:

```
1. plsql2java-discovery      (no internal deps)
2. plsql2java-translation    (depends on: discovery)
3. plsql2java-codegen        (depends on: discovery, translation)
4. plsql2java-orchestrator   (depends on: discovery, translation, codegen)
5. plsql2java-cli            (depends on: orchestrator)
6. plsql2java-web            (depends on: orchestrator)
```

---

## Integration Points Between Units

| From Unit | To Unit | Integration Point |
|---|---|---|
| Unit 1 → Unit 2 | OracleObject passed to PlSqlTranslationEngine.translate() | Domain model |
| Unit 1 → Unit 3 | DiscoveryResult + DependencyGraph passed to report generator | Domain model |
| Unit 2 → Unit 3 | TranslationResult passed to JavaCodeGeneratorService + ConfidenceScorerService | Domain model |
| Unit 3 → Unit 4 | GeneratedProject + ConfidenceReport + MigrationReport returned to orchestrator | Domain model |
| Unit 4 → Unit 5 | MigrationOrchestratorService.analyze/generate/report() called by CLI commands | Service interface |
| Unit 4 → Unit 6 | MigrationOrchestratorService.analyze/generate/report() called by REST controllers | Service interface |
| Unit 4 → Unit 5/6 | ProgressEvent emitted by orchestrator, consumed by CLI/Web listeners | Event bus |

---

## Coordination Requirements

- **Shared domain models** (OracleObject, DiscoveryResult, TranslationResult, etc.) are defined in Unit 1 (`plsql2java-discovery`) and used by all downstream units — changes to these models require coordinated updates
- **MigrationConfig** is defined in Unit 4 (`plsql2java-orchestrator`) and consumed by Units 5 and 6 — interface stability is critical before Units 5/6 begin
- **ProgressEvent** interface is defined in Unit 1 (common infrastructure) — all units emit events using this contract
