# Story 1.3 — Object Type Discovery

**Epic**: Epic 1: Oracle Schema Discovery
**Story Number**: 1.3
**Source**: GitHub Issue

---

**Epic**: Epic 1: Oracle Schema Discovery
**Story**: 1.3
**Source**: `aidlc-docs/inception/user-stories/stories.md`

---

**As a** Database Engineer,
**I want to** see all discovered Oracle object types categorized by type,
**so that** I can validate that the tool has found all relevant objects in the schema.

**Acceptance Criteria — Happy Path:**
- Given a schema has been successfully connected or imported
- When discovery completes
- Then the tool reports counts and names of all discovered: packages (spec + body), standalone procedures, standalone functions, triggers, views, sequences, and user-defined types

**Acceptance Criteria — Edge Cases & NFRs:**
- [ ] Discovery coverage is ≥95% of all objects present in the schema
- [ ] Objects with compilation errors in the source schema are flagged (not silently skipped)
- [ ] Package spec and body are linked and reported as a single package unit
- [ ] Discovery results are persisted so they can be referenced in subsequent generate/report commands

---

---

*Auto-generated from GitHub Issue by sync-issue-to-docs workflow.*
