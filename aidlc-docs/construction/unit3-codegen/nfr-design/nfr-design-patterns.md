# NFR Design Patterns — Unit 3: Code Generator + Confidence Scorer + Report Generator

---

## Pattern 1: Externalized Templates (FreeMarker)

**Addresses**: NFR-CG-M1 (maintainability — no hardcoded Java source strings)

All Java artifact templates are FreeMarker `.ftl` files in `src/main/resources/templates/`. The generator loads templates via `Configuration.getTemplate()` and processes them with a data model map. Adding a new artifact type requires only a new template — no changes to service logic.

---

## Pattern 2: Fail-Partial Generation

**Addresses**: NFR-CG-R1 (reliability)

Mirrors Units 1 and 2:
- `generateProject()` wraps each object's generation in try/catch
- Failed objects are logged at WARN, added to a `skippedObjects` list in GeneratedProject
- Generation continues for all remaining objects
- Final GeneratedProject always returned (never null)

---

## Pattern 3: Try-With-Resources for File I/O

**Addresses**: SECURITY-15, NFR-CG-S5

All file write operations use try-with-resources with `BufferedWriter`:
```java
try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
    writer.write(content);
}
```

---

## Pattern 4: Output Directory Path Validation

**Addresses**: SECURITY-05, NFR-CG-S6

Before writing any file, the resolved absolute path is validated:
- Must be within the configured `outputDir` (no `../` traversal)
- `resolvedPath.startsWith(outputDir.toRealPath())` check enforced

---

## Pattern 5: MDC Structured Logging

**Addresses**: SECURITY-03, NFR-CG-S4

Consistent with Units 1 and 2:
- `MDC.put("objectName", ...)` before generating each object
- `MDC.clear()` in finally block
- No credentials, PL/SQL source, or connection strings in log output

---

## Pattern 6: Score Clamping

**Addresses**: BR-CS01 (score range 0–100)

All confidence score calculations pass through `Math.max(0, Math.min(100, rawScore))` before assignment. No score can escape the valid range regardless of penalty accumulation.

---

## Pattern 7: Section-Based Report Assembly

**Addresses**: NFR-CG-M2 (independently renderable sections)

Each report section is a private method returning a String:
- `buildExecutiveSummary()`, `buildTraceabilitySection()`, `buildFlaggedConstructsSection()`, etc.
- Sections are assembled in order by `generateReport()`
- Both Markdown and HTML renderers call the same section-building methods with format-specific wrappers
