# Migration Report — UNKNOWN

**Schema**: UNKNOWN  
**Migration Date**: 2026-08-06T13:27:39Z  
**Tool Version**: 1.0.0  

## Executive Summary

| Metric | Value |
|---|---|
| Total Objects Discovered | 5 |
| Objects Translated | 0 |
| Objects Partial | 0 |
| Objects Flagged | 5 |
| Objects Skipped | 0 |
| Traceability Coverage | 0.0% |
| Flagged Constructs | 5 |
| Overall Confidence Score | 70% |

## Traceability Matrix

| PL/SQL Object | Type | Java Class | Methods | Status | Confidence |
|---|---|---|---|---|---|
| PKG_CUSTOMER | PACKAGE | PkgCustomerServiceService | 1 | FLAGGED | 70% |
| SEQ_CUSTOMER | SEQUENCE | SeqCustomerServiceService | 1 | FLAGGED | 70% |
| SEQ_KYC | SEQUENCE | SeqKycServiceService | 1 | FLAGGED | 70% |
| TRG_CUSTOMER | TRIGGER | TrgCustomerServiceService | 1 | FLAGGED | 70% |
| TRG_KYC | TRIGGER | TrgKycServiceService | 1 | FLAGGED | 70% |

**Coverage**: 0.0%

## Flagged Constructs

### UNKNOWN

| Object | Line | Reason | Recommendation |
|---|---|---|---|
| PKG_CUSTOMER | 5 | No translation rule for construct: UNKNOWN | Review this construct manually |
| SEQ_CUSTOMER | 5 | No translation rule for construct: UNKNOWN | Review this construct manually |
| SEQ_KYC | 1 | No translation rule for construct: UNKNOWN | Review this construct manually |
| TRG_CUSTOMER | 5 | No translation rule for construct: UNKNOWN | Review this construct manually |
| TRG_KYC | 1 | No translation rule for construct: UNKNOWN | Review this construct manually |

## Dependency Graph Summary

- **Circular Dependencies**: 0
- **Leaf Objects**: 5
- **Migration Order (first 10)**: PKG_CUSTOMER, SEQ_CUSTOMER, SEQ_KYC, TRG_CUSTOMER, TRG_KYC

## Confidence Scores

| Object | Type | Score | Status |
|---|---|---|---|
| PKG_CUSTOMER | PACKAGE | 70% | ✓ OK |
| SEQ_CUSTOMER | SEQUENCE | 70% | ✓ OK |
| SEQ_KYC | SEQUENCE | 70% | ✓ OK |
| TRG_CUSTOMER | TRIGGER | 70% | ✓ OK |
| TRG_KYC | TRIGGER | 70% | ✓ OK |

