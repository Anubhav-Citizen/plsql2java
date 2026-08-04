# Unit of Work Plan
## plsql2java — Oracle PL/SQL Legacy Modernization Platform

Please answer each question by filling in the letter choice after the `[Answer]:` tag.
Let me know when you're done.

---

## Part 1: Decomposition Questions

---

## Question 1
How should the 9 components be grouped into units of work for development?

A) By architectural layer — 3 units: (1) Core Engine (Discovery + Dependency + Translation + Scoring), (2) Output Layer (Code Generator + Report Generator), (3) Delivery Layer (CLI + Web App + Orchestrator)
B) By delivery mode — 2 units: (1) CLI-focused (all engine + CLI), (2) Web App (Web Application Component only, reusing engine from Unit 1)
C) By pipeline stage — 4 units: (1) Discovery + Dependency, (2) Translation Engine, (3) Code Generator + Scoring + Reporting, (4) CLI + Web Delivery
D) Other (please describe after [Answer]: tag below)

[Answer]: C

---

## Question 2
Should the Migration Orchestrator Service be its own unit or bundled with another unit?

A) Bundle with the Core Engine unit — it's the glue between engine components
B) Bundle with the Delivery unit — it's the entry point for CLI and Web
C) Its own unit — developed after core engine, before delivery
D) Other (please describe after [Answer]: tag below)

[Answer]: C

---

## Question 3
Should CLI and Web Application be developed as separate units or together?

A) Separate units — CLI first (simpler, validates the engine), Web App second
B) Together as one Delivery unit — they share the same Orchestrator interface
C) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Question 4
What is the preferred Maven project structure for the plsql2java tool itself?

A) Single Maven module (all components in one module, organized by package)
B) Multi-module Maven project (each unit/layer as a separate Maven module with explicit dependencies)
C) Other (please describe after [Answer]: tag below)

[Answer]: B

---

## Part 2: Generation Plan

Once questions are answered, the following steps will be executed:

- [x] Step 1: Define units of work with names, components, and responsibilities
- [x] Step 2: Define unit dependencies (which units must be built before others)
- [x] Step 3: Map user stories to units
- [x] Step 4: Document code organization strategy (Maven structure, package layout)
- [x] Step 5: Generate unit-of-work.md
- [x] Step 6: Generate unit-of-work-dependency.md
- [x] Step 7: Generate unit-of-work-story-map.md

---
