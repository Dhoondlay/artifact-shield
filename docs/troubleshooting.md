# 🔧 Artifact-Shield Troubleshooting Guide

This guide describes the most common issues you'll encounter as a developer or operator of the Artifact-Shield gateway.

---

## 🔒 1. SSL & mTLS Failures
**Scenario**: Handshake error when calling a downstream LLM.

### **The Cause**
-   The `.p12` file path in the database is incorrect.
-   The password for the keystore/truststore is invalid.
-   The downstream server's certificate is not trusted by our Root CA.

### **The Fix**
1.  **Check Paths**: Ensure the `keystore_path` in `shield_downstream_configs` is absolute or relative to the working directory.
2.  **Verify Trust**:
    ```bash
    keytool -list -v -keystore ./certs/trust.p12
    ```
3.  **Debug Handshake**: Enable verbose SSL logging:
    ```bash
    java -Djavax.net.debug=all -jar app.jar
    ```

---

## 📁 2. H2 Database Lock
**Scenario**: "Database may be already in use" when starting the application.

### **The Cause**
-   The application was stopped abruptly, and the H2 file lock (`.lock.db`) was not released.
-   Another instance of the app (or a DB client) is accessing the `shielddb.mv.db` file.

### **The Fix**
1.  **Check Processes**: Kill any java processes still running.
2.  **Delete Lock File**: Manually delete `~/shielddb.lock.db`.

---

## 🚫 3. Swagger UI (OpenAPI) 404
**Scenario**: `http://localhost:8080/swagger-ui.html` returns a 404.

### **The Cause**
-   Springdoc version incompatibility with WebFlux (resolved in `2.5.0`+).
-   The reactive Security Filter is blocking the `/swagger-ui/**` path.

### **The Fix**
1.  **Update Security**: Inside `SecurityConfig.java`, ensure you have:
    ```java
    .pathMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
    ```
2.  **Clear Cache**: Browsers sometimes cache the 404 for `/swagger-ui/index.html`. Open in a private window.

---

## 📉 4. Low Throughput / Event-Loop Blocking
**Scenario**: High latency even for small requests.

### **The Cause**
-   **Blocking I/O**: You or a new contributor added a blocking call (like `Files.readString()`) on the main event loop.
-   **Insufficient Memory**: The Java Heap is too small for large redaction regexes.

### **The Fix**
1.  **Check BlockHound**: Integrate **BlockHound** into your tests to detect blocking calls automatically.
2.  **Increase Heap**: Use `-Xmx2G` or higher for high-concurrency production loads.

---

## 🚀 5. "401 Unauthorized" from Gemini/OpenAI
**Scenario**: Proxied LLM call fails even after redaction.

### **The Cause**
-   The `auth_token` in `shield_downstream_configs` has expired.
-   The LLM provider's token format has changed.

### **The Fix**
1.  **Test Token**: Use `curl` to test the token directly from the gateway server:
    ```bash
    curl -H "Authorization: Bearer YOUR_TOKEN" https://api.openai.com/v1/...
    ```
2.  **Update DB**: Update the token at runtime using the SQL:
    ```sql
    UPDATE shield_downstream_configs SET auth_token = 'new_token' WHERE alias = 'gemini';
    ```

6.  **"CORS Error" in Admin Dashboard**
**Scenario**: The dashboard loads, but statistics and patterns show "Connection Refused" or "CORS Error".

### **The Cause**
-   The gateway has `shield.security.enabled: true` but `shield.security.cors-enabled` is `false`.
-   The browser is blocking the request because the dashboard is on a different domain/port.

### **The Fix**
1.  **Enable CORS**: In `application.yml`, set `shield.security.cors-enabled: true`.
2.  **Origin Policy**: If you are using a reverse proxy (like Nginx), ensure headers are properly propagated:
    ```nginx
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    ```

---

## 🕒 7. Downstream LLM Timeouts
**Scenario**: Sanitization works, but `llmResponse` is "Error: Read Timeout".

### **The Cause**
-   The downstream LLM (e.g., GPT-4) is taking too long to generate a response for a large prompt.
-   The gateway's `WebClient` timeout is set too low (Default: 30s).

### **The Fix**
1.  **Increase Timeout**: If you are using a custom `DownstreamProxyClient`, you can increase the `WebClient` response timeout.
2.  **Check Upstream Status**: Verify if the LLM provider is currently experiencing an outage.

---

## 📂 8. Rapid Log File Growth
**Scenario**: The Gateway's disk space is filling up with log files.

### **The Cause**
-   `Logging` level is set to `DEBUG` or `TRACE` in production.
-   The gateway is processing thousands of requests per minute with verbose auditing.

### **The Fix**
1.  **Adjust Level**: In `application.yml`, ensure logging is set to `INFO` or `WARN` for production:
    ```yaml
    logging:
      level:
        io.dhoondlay.shield: INFO
        org.springframework: WARN
    ```
2.  **Configure Rotation**: Use a standard `logback-spring.xml` to enable daily log rotation and compression.

---

## 🚫 9. H2 Console "403 Forbidden"
**Scenario**: Clicking the H2 Console link in the dashboard shows a white screen or a "403" error.

### **The Cause**
-   Spring Security is blocking the `/h2-console/**` path.
-   CSRF protection is enabled and blocking the H2 console’s frames.

### **The Fix**
1.  **Permit Access**: In `SecurityConfig.java`, ensure `/h2-console/**` is in the `permitAll()` list.
2.  **Disable Frame Protection**: The H2 console requires frame-same-origin to function. Artifact-Shield handles this automatically in the default `SecurityConfig`, so verify your custom rules hasn't overridden it.

---

## 🔢 10. Missing Traceability (No Correlation ID)
**Scenario**: Splunk is receiving logs, but multiple log lines for the same request are not linked.

### **The Cause**
-   The `CorrelationIdFilter` is not being executed (often due to being disabled in a custom filter chain).
-   MDC (Mapped Diagnostic Context) is lost because a library was used that doesn't support Reactor Context.

### **The Fix**
1.  **Check Filter Order**: Ensure `CorrelationIdFilter` is at the beginning of the `WebFilter` chain.
2.  **Reactor Context**: Ensure you are using `ReactiveSecurityContextHolder` or similar to stay within the reactive pipeline logic.

## 🔍 11. My Regex Pattern is not redacting
**Scenario**: You added a new rule for `PASSWORD` but it's not being replaced in the text.

### **The Cause**
-   **Case Sensitivity**: The regex is sensitive (e.g., matching `password` but not `Password`).
-   **Lookarounds**: You used complex lookarounds that are not supported by the standard Java regex engine in a streaming context.
-   **Detector disabled**: The parent detector category is disabled in `application.yml`.

### **The Fix**
1.  **Test the Pattern**: Use a tool like **Regex101** (Java mode) with your string.
2.  **Enable Flags**: Add `(?i)` to the beginning of your regex to make it case-insensitive.
3.  **Check Category**: Verify `shield.detectors.<category>.enabled` is `true`.

---

## 🏗️ 12. "Table not found" after migrating to Postgres
**Scenario**: You switched to PostgreSQL but the app says `relation "shield_patterns" does not exist`.

### **The Cause**
-   Hibernate `ddl-auto` is set to `none` or `validate`.
-   The Postgres user doesn't have permission to create tables in the schema.

### **The Fix**
1.  **Grant Permissions**: Ensure the user is a `superuser` or has `CREATE` rights.
2.  **Set Auto-DDL**: Temporarily set `spring.jpa.hibernate.ddl-auto: update` to let the app build the schema on its first run.

---
*For more help, contact your security engineer or visit the [Artifact-Shield internal wiki].*
