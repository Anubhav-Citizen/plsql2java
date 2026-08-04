# Logical Components — Unit 1: Discovery & Dependency Analysis

Unit 1 is a pure Java library module with no external infrastructure dependencies (no database of its own, no message queue, no cache). All logical components are in-process Java classes.

---

## Logical Component 1: JdbcConnectionManager

**Purpose**: Manages Oracle JDBC connection lifecycle
**Pattern**: Factory + try-with-resources
**Responsibilities**:
- Create DataSource from JdbcConfig (using Oracle JDBC driver)
- Enforce connection timeout
- Provide connections to OracleDiscoveryService via DataSource

---

## Logical Component 2: OracleDataDictionaryReader

**Purpose**: Executes Oracle data dictionary queries to retrieve object metadata and source
**Pattern**: Repository (read-only)
**Responsibilities**:
- Execute ALL_OBJECTS, ALL_SOURCE, ALL_VIEWS, ALL_SEQUENCES, ALL_ERRORS queries
- Map ResultSet rows to raw data structures
- Use batch fetch size (100) for performance

---

## Logical Component 3: DdlFileParser

**Purpose**: Parses Oracle DDL/SQL export files into OracleObject instances
**Pattern**: Parser + Strategy (one strategy per object type)
**Responsibilities**:
- Tokenize DDL file into individual statements
- Classify each statement by object type using DdlPatterns
- Extract object name and source text
- Validate file paths (path traversal prevention)

---

## Logical Component 4: OracleObjectNormalizer

**Purpose**: Normalizes raw discovery data (from JDBC or file) into OracleObject domain model
**Pattern**: Mapper
**Responsibilities**:
- Merge PACKAGE spec and PACKAGE BODY into a single OracleObject
- Uppercase object names (Oracle convention)
- Set hasCompilationErrors flag
- Calculate lineCount

---

## Logical Component 5: DependencyGraphBuilder

**Purpose**: Builds the dependency graph from a list of OracleObjects
**Pattern**: Builder
**Responsibilities**:
- Scan object source for cross-references using regex patterns
- Build adjacency list
- Filter out external references (only include objects in DiscoveryResult)

---

## Logical Component 6: CycleDetector

**Purpose**: Detects circular dependencies in the dependency graph
**Pattern**: Graph algorithm (DFS with node coloring)
**Responsibilities**:
- Run DFS on adjacency list
- Detect and record all cycles
- Return List\<CircularDependency\>

---

## Logical Component 7: TopologicalSorter

**Purpose**: Computes leaf-first migration order
**Pattern**: Graph algorithm (Kahn's algorithm)
**Responsibilities**:
- Compute in-degrees for all nodes
- Run BFS-based topological sort
- Append circular dependency nodes at end
- Return ordered List\<String\>

---

## Logical Component 8: ResultPersistenceService

**Purpose**: Serializes and deserializes DiscoveryResult and DependencyGraph to/from JSON
**Pattern**: Repository (read/write)
**Responsibilities**:
- Serialize to `{outputDir}/discovery-result.json` and `{outputDir}/dependency-graph.json`
- Deserialize for generate/report modes
- Create output directory if it doesn't exist
- Write human-readable `analysis-summary.md`

---

## Logical Component 9: ProgressEventEmitter

**Purpose**: Emits ProgressEvents to registered listeners
**Pattern**: Observer
**Responsibilities**:
- Hold reference to ProgressListener (injected)
- Calculate percentComplete
- Emit events after each object is processed
