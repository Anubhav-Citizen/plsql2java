# Story 6.3 — Flagged Constructs Report

**Epic**: Epic 6: Migration Reporting
**Story Number**: 6.3
**Source**: GitHub Issue

---

**Epic**: Epic 6: Migration Reporting
**Story**: 6.3
**Source**: `aidlc-docs/inception/user-stories/stories.md`

---

**As a** Database Engineer,
**I want** a detailed list of all flagged Oracle constructs in the migration report,
**so that** I can provide guidance to the Java team on how to handle each unsupported pattern.

**Acceptance Criteria — Happy Path:**
- Given migration has completed and some constructs were flagged
- When I view the flagged constructs section
- Then each flagged construct is listed with: object name, construct type, original PL/SQL line reference, and a migration recommendation

**Acceptance Criteria — Edge Cases & NFRs:**
- [ ] Recommendations are actionable (e.g., "Replace DBMS_SCHEDULER with Spring @Scheduled")
- [ ] Flagged constructs are grouped by construct type for easy scanning
- [ ] If no constructs are flagged, the section states "No unsupported constructs found"

---

---

*Auto-generated from GitHub Issue by sync-issue-to-docs workflow.*
