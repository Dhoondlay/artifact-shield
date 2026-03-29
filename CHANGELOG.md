# 📜 Artifact-Shield Changelog

All notable changes to **Artifact-Shield** will be documented in this file.

---

## [1.2.0-REACTIVE] - 2026-03-29 (Latest)
**Major Release: Performance & Scalability**

### 🚀 **Added**
-   **Fully Reactive Architecture**: Migrated core pipeline and controllers to Spring WebFlux & Project Reactor.
-   **Non-Blocking Proxy Client**: Implemented `WebClient` for high-concurrency LLM calls.
-   **Correlation ID Propagation**: Added `CorrelationIdFilter` to trace requests across non-blocking thread boundaries.
-   **Security Hardening**: Added optional `OAuth2/JWT` Resource Server support.
-   **Enhanced Scanners**: 
    -   `FinancialDetector`: Now includes the **Luhn Algorithm** for deterministic credit card validation.
    -   `CredentialDetector`: Expanded patterns for OpenAI, Azure, and AWS keys.
-   **Audit Compliance**: Asynchronous audit logging for zero latency impact.

### 🛡️ **Improved**
-   **Global Exception Handling**: Added suppressing for 404 noise (favicon/static assets) in reactive environments.
-   **SpringDoc Upgrade**: Updated to `v2.5.0` for full WebFlux compatibility.

### 🔧 **Fixed**
-   Resolved blocking I/O calls in `RedactionService` by offloading them to `Schedulers.boundedElastic()`.

---

## [1.1.0-ENTERPRISE] - 2026-03-10
**Initial SQL-Driven Configuration**

### 🚀 **Added**
-   **Dynamic Database Rules**: Patterns and Downstream configs moved from `application.yml` to SQL tables.
-   **mTLS Support**: Initial implementation of mTLS via keystore/truststore configuration for downstream LLMs.
-   **Admin API**: Initial endpoints for managing rules at runtime.

---

## [1.0.0-MVP] - 2026-02-15
**Initial Release (Synchronous)**

### 🚀 **Added**
-   **Simple Redaction**: Basic regex-based redaction for PII.
-   **Servlet Gateway**: Synchronous (blocking) HTTP proxy.

---
*For more help, contact your security engineer or visit the [Artifact-Shield internal wiki].*
