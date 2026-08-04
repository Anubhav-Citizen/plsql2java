# Business Logic Model — Unit 1: Discovery & Dependency Analysis

---

## Workflow 1: JDBC Schema Discovery

**Trigger**: User provides JdbcConfig (URL, username, password)
**Output**: DiscoveryResult

### Steps:
1. Validate JdbcConfig (non-blank URL, username, password; valid URL format)
2. Establish JDBC connection with configured timeout
3. Query `ALL_OBJECTS` to get list of all objects in the target schema filtered by supported types
4. For each object, query `ALL_SOURCE` to retrieve full source code (spec and body separately)
5. For SEQUENCE objects, query `ALL_SEQUENCES` for metadata (no source code)
6. For VIEW objects, query `ALL_VIEWS` for the view definition text
7. For TYPE objects, query `ALL_SOURCE` for both spec and body
8. Detect compilation errors by checking `ALL_ERRORS` for each object
9. Normalize each result into an OracleObject
10. Emit ProgressEvent after each object is discovered
11. Close JDBC connection (always — even on error)
12. Return DiscoveryResult

**Oracle Queries Used**:
- `SELECT OBJECT_NAME, OBJECT_TYPE FROM ALL_OBJECTS WHERE OWNER = :schema AND OBJECT_TYPE IN ('PACKAGE','PACKAGE BODY','PROCEDURE','FUNCTION','TRIGGER','VIEW','SEQUENCE','TYPE','TYPE BODY')`
- `SELECT TEXT FROM ALL_SOURCE WHERE OWNER = :schema AND NAME = :name AND TYPE = :type ORDER BY LINE`
- `SELECT TEXT FROM ALL_VIEWS WHERE OWNER = :schema AND VIEW_NAME = :name`
- `SELECT * FROM ALL_SEQUENCES WHERE SEQUENCE_OWNER = :schema AND SEQUENCE_NAME = :name`
- `SELECT COUNT(*) FROM ALL_ERRORS WHERE OWNER = :schema AND NAME = :name AND TYPE = :type`

---

## Workflow 2: DDL File Discovery

**Trigger**: User provides one or more DDL/SQL file paths
**Output**: DiscoveryResult

### Steps:
1. Validate each file path exists and is readable
2. Validate file extension is .sql or .ddl (reject others with clear error)
3. Validate file size does not exceed maximum (configurable, default: 50MB per file)
4. For each file:
   a. Read file content with charset detection (UTF-8 first, fallback ISO-8859-1)
   b. Tokenize content into individual DDL statements (split on `/` or `;` delimiters)
   c. For each statement, identify the object type and name using regex patterns:
      - `CREATE OR REPLACE PACKAGE <name>` → PACKAGE
      - `CREATE OR REPLACE PACKAGE BODY <name>` → PACKAGE_BODY
      - `CREATE OR REPLACE PROCEDURE <name>` → PROCEDURE
      - `CREATE OR REPLACE FUNCTION <name>` → FUNCTION
      - `CREATE OR REPLACE TRIGGER <name>` → TRIGGER
      - `CREATE OR REPLACE VIEW <name>` → VIEW
      - `CREATE SEQUENCE <name>` → SEQUENCE
      - `CREATE OR REPLACE TYPE <name>` → TYPE
   d. Extract object name and source text
   e. Normalize into OracleObject
   f. Emit ProgressEvent after each object is parsed
5. Collect all DiscoveryErrors for malformed statements
6. Return DiscoveryResult (including objects from all files)

---

## Workflow 3: Dependency Analysis

**Trigger**: DiscoveryResult available
**Output**: DependencyGraph

### Steps:
1. Initialize empty adjacency list (Map\<String, Set\<String\>\>)
2. For each OracleObject in DiscoveryResult:
   a. Scan sourceSpec and sourceBody for cross-object references using pattern matching:
      - Procedure/function calls: `<schema>.<object_name>(` or `<object_name>(`
      - Package member calls: `<package_name>.<procedure_name>(`
      - View references in FROM clauses: `FROM <view_name>`
      - Type references: `: <type_name>` or `<type_name>%TYPE`
      - Trigger table references: `ON <table_or_view_name>`
   b. For each reference found, add a DependencyEdge (fromObject → toObject)
   c. Only add edges where toObject exists in the DiscoveryResult (ignore external references)
3. Run cycle detection (DFS with WHITE/GRAY/BLACK node coloring)
4. For each detected cycle, create a CircularDependency record
5. Run topological sort (Kahn's algorithm) on the acyclic portion of the graph
6. Objects in circular dependencies are appended at the end of migrationOrder with a warning flag
7. Identify leafObjects (objects with in-degree = 0 in the dependency graph)
8. Return DependencyGraph

---

## Workflow 4: Result Persistence

**Trigger**: DiscoveryResult or DependencyGraph available
**Output**: Files written to output directory

### Steps:
1. Serialize DiscoveryResult to `{outputDir}/discovery-result.json`
2. Serialize DependencyGraph to `{outputDir}/dependency-graph.json`
3. Write human-readable summary to `{outputDir}/analysis-summary.md`
4. Verify files are written successfully (check file size > 0)

---

## Workflow 5: Result Loading (for generate/report modes)

**Trigger**: User runs generate or report command with existing output directory
**Output**: DiscoveryResult + DependencyGraph loaded into memory

### Steps:
1. Check `{outputDir}/discovery-result.json` exists — fail with clear error if not
2. Deserialize DiscoveryResult from JSON
3. Check `{outputDir}/dependency-graph.json` exists — fail with clear error if not
4. Deserialize DependencyGraph from JSON
5. Validate loaded data is not empty/corrupt
6. Return both objects to caller

---

## Algorithm: Cycle Detection (DFS)

```
Input: adjacency list (Map<String, Set<String>>)
Output: List<CircularDependency>

For each unvisited node:
  DFS with three states: WHITE (unvisited), GRAY (in current path), BLACK (fully visited)
  When a GRAY node is encountered during DFS → cycle detected
  Record the cycle path from the GRAY node back to itself
  Mark all nodes in cycle as BLACK after recording
```

---

## Algorithm: Topological Sort (Kahn's Algorithm)

```
Input: adjacency list (excluding circular dependency nodes)
Output: List<String> in leaf-first order

1. Compute in-degree for each node
2. Initialize queue with all nodes having in-degree = 0 (leaf nodes)
3. While queue is not empty:
   a. Dequeue node, add to result
   b. For each neighbor: decrement in-degree; if in-degree = 0, enqueue
4. Append circular dependency nodes at end with warning
5. Return result
```
