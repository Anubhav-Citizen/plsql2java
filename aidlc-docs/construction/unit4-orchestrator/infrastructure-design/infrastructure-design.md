# Infrastructure Design — Unit 4: Migration Orchestrator

---

## Infrastructure Profile: Pure Library Module

Unit 4 is a pure Spring library module — no web server, no database, no external infrastructure.

| Concern | Decision |
|---|---|
| Deployment | Packaged as JAR; consumed by Units 5 (CLI) and 6 (Web) |
| Persistence | File system only — JSON serialization via Jackson to outputDir |
| Messaging | In-process ProgressEventBus — no broker required |
| Database | None — orchestrator delegates to core engine services |
| External services | None — all calls are in-process |

---

## File System Layout (outputDir)

```
{outputDir}/
  analysis/
    discovery-result.json
    dependency-graph.json
  generated/
    {project-name}/        (Maven project files)
  reports/
    report.md
    report.html
  migration-result.json    (full serialized MigrationResult)
```

---

## Spring Auto-Configuration

- `OrchestratorAutoConfiguration` registered via `META-INF/spring/` imports file
- Scans `com.plsql2java.orchestration` package
- Depends on beans from DiscoveryAutoConfiguration, TranslationAutoConfiguration, CodegenAutoConfiguration
