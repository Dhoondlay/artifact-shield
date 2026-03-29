# 🌟 Artifact-Shield: Core Enterprise Features

Artifact-Shield is more than a simple redaction tool; it is a full-featured security gateway for the AI-first enterprise. This document explores each of our core pillars in detail.

---

## ⚡ 1. Fully Reactive & Non-Blocking Proxy
**Key Feature**: High-concurrency support with zero resource starvation.

### **The Benefit**
Unlike traditional "blocking" gateways (Servlet-based), Artifact-Shield is built on **Spring WebFlux and Project Reactor**. Every incoming request, database query, and outbound LLM call is handled asynchronously.

### **How it Works**
-   Incoming requests are processed on a small, constant number of "event loop" threads.
-   Blocking tasks (like H2 or Postgres queries) are offloaded to a **BoundedElastic Scheduler**.
-   This architecture allows the gateway to handle thousands of concurrent AI prompts (which often have long latencies) without crashing.

---

## 🛡️ 2. Deterministic & Zero-Hallucination Detection
**Key Feature**: Non-AI security logic.

### **The Benefit**
Using an LLM to "find PII" is risky because LLMs can hallucinate or miss subtle patterns. Artifact-Shield uses **Deterministic Detectors**.

### **How it Works**
-   **Regex Detectors**: High-performance, optimized regular expressions for Emails, IP Addresses, and Secrets.
-   **Mathematical Validation**: The `FinancialDetector` uses the **Luhn Algorithm** to cross-reference credit card numbers, ensuring only valid 16-digit sequences are redacted.

---

## 🗄️ 3. Dynamic Rule Engine (Database-Driven)
**Key Feature**: Real-time policy updates.

### **The Benefit**
Security threats evolve faster than application release cycles. Artifact-Shield stores its detection patterns and downstream LLM configs in a database.

### **How it Works**
-   **Runtime Updates**: Change a regex or add a new LLM endpoint in the `shield_patterns` or `shield_downstream_configs` table.
-   **Immediate Effect**: The gateway picks up these changes for the very next request without requiring a restart.

---

## 🔐 4. mutual TLS (mTLS) & SSL Support
**Key Feature**: Secure end-to-end communication.

### **The Benefit**
When talking to an internal enterprise LLM service, standard HTTPS is often not enough. Artifact-Shield supports mTLS for identity verification.

### **How it Works**
-   **Identity (Keystore)**: The gateway presents its own certificate to the LLM.
-   **Trust (Truststore)**: The gateway verifies the LLM server's certificate against a private root CA.
-   **Configuration**: Simply provide the local path to your `.p12` files in the database config, and the gateway handles the handshake automatically.

---

## 📊 5. Audit Compliance & Forensics
**Key Feature**: Comprehensive transaction visibility.

### **The Benefit**
Security teams need to know *what* was detected and *when*, without storing the actual sensitive data (zero-pii logging).

### **How it Works**
-   **Metadata Logging**: Every request is logged with its risk score, severity, detected patterns, and processing latency.
-   **Anonymized Content**: The audit log stores the `sanitizedText`, never the `rawText`.
-   **Splunk Integration**: Optionally stream these logs to Splunk for real-time alerting.

---

## 🖥️ 6. Real-Time Admin Dashboard
**Key Feature**: Visual security operations.

### **The Benefit**
Provides a "Single Pane of Glass" for security administrators.

### **How it Works**
-   **Overview**: Monitor total request volume and top detection categories.
-   **Management**: Enable/Disable patterns or endpoints with a single click.
-   **Logs**: Browse the audit trail for anomalies directly from the browser.

---

## 🔑 7. Enterprise Security (OAuth2/JWT)
**Key Feature**: Protecting the protector.

### **The Benefit**
Ensures that only authorized applications can send text to the gateway.

### **How it Works**
-   **Optional Security**: Toggle `shield.security.enabled: true`.
-   **Standard Implementation**: Uses Spring Security (WebFlux) to validate JWT tokens from providers like Azure AD, Okta, or Keycloak.
-   **Granular Access**: Protects the `/v1/shield/sanitize` endpoint behind enterprise-grade authentication.

---

## 🔍 8. Deterministic Security vs. AI Guessing
**Key Feature**: Zero Hallucinations.

### **The Challenge**
Modern AI-based security tools often "guess" where PII is by using another LLM to scan the text. This is slow, expensive, and prone to hallucinations.

### **The Solution**
Artifact-Shield uses **Deterministic Detectors**:
-   **Regex Precision**: Patterns are optimized for Java's regex engine.
-   **Luhn's Algorithm**: For credit card validation, it doesn't just look for 16 digits; it mathematically verifies that the sequence is a valid card number, reducing false positives by 99%.
-   **Secret Scanners**: Pre-built rules for **AWS Access Keys**, **OpenAI Tokens**, and **Azure Secrets**.

---

## 🚠 9. Distributed Tracing (Correlation IDs)
**Key Feature**: End-to-End Visibility.

### **The Benefit**
In a microservices architecture, tracking a single user's request across the gateway and into the LLM can be difficult.

### **The How**
-   **Automatic Injection**: The gateway assigns an `X-Correlation-ID` to every inbound request.
-   **Reactive Propagation**: Unlike traditional apps that use `ThreadLocal`, we use **Reactor Context** to ensure the ID is carried across every asynchronous thread.
-   **Log Correlation**: Search for a single ID in your logs to see the initial prompt, the redaction result, and the final LLM response as a unified "story."

---

## 🚀 10. Reactive Performance (WebFlux)
**Key Feature**: Infinite Scalability.

### **The Architecture**
Artifact-Shield is not a standard Spring Boot app; it is a **Reactive Netty Server**.
-   **Traditional App**: 1 Request = 1 Thread. If you have 500 requests waiting for an LLM (which takes 10s), you need 500 threads. This crashes most servers.
-   **Artifact-Shield**: 10,000+ Requests = ~10 Event Loop Threads. Because we use non-blocking I/O, our threads never "wait." They switch back and forth between requests, keeping CPU usage low even under extreme load.

---
*For more help, contact your security engineer or visit the [Artifact-Shield internal wiki].*
