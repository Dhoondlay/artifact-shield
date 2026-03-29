# 📖 API Reference

Artifact-Shield provides a non-blocking, reactive REST API for text sanitization, risk assessment, and LLM proxying. All endpoints follow standard REST conventions and return JSON.

---

## 🛡️ 1. Shield API (The Core Pipeline)

The primary interface for cleaning data and communicating with LLMs.

### `POST /v1/shield/sanitize`
The workhorse endpoint. It scans your text, applies redaction, and optionally forwards the result to a downstream LLM.

#### **Request Body**
| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `content` | String | Yes | The raw text to scan/redact. |
| `action` | String | No | One of: `ANALYZE` (score only), `REDACT` (return clean text), `FORWARD` (call LLM). Default: `REDACT`. |
| `forwardTo` | String | No | The alias of the downstream LLM (e.g., `openai`, `gemini`) if action is `FORWARD`. |

#### **Example Request**
```json
{
  "content": "My AWS Key is AKIA1234567890ABCDEF",
  "action": "FORWARD",
  "forwardTo": "gemini"
}
```

#### **Response Body**
| Field | Type | Description |
| :--- | :--- | :--- |
| `sanitizedText` | String | The text after placeholders have been injected. |
| `llmResponse` | String | The raw response from the LLM (if proxied, otherwise null). |
| `severity` | Enum | `CLEAN`, `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`. |
| `riskScore` | Integer | Calculated risk from 0 to 100. |
| `detections` | Array | List of specific patterns found (e.g., `AWS_KEY`). |
| `latencyMs` | Long | Total round-trip time in milliseconds. |

---

## 🏗️ 2. Admin API (Management)

Manage rules, endpoints, and audit trails at runtime. All `/api/admin/**` endpoints are intended for internal use or authorized dashboards.

### **Rule Management**
*   **`GET /api/admin/patterns`**: List all active redaction patterns.
*   **`POST /api/admin/patterns`**: Create or update a regex pattern.
*   **`PATCH /api/admin/patterns/{id}/toggle`**: Enable/Disable a rule without deleting it.
*   **`DELETE /api/admin/patterns/{id}`**: Remove a rule permanently.

### **Downstream Management**
*   **`GET /api/admin/downstreams`**: List all LLM endpoints and their status.
*   **`POST /api/admin/downstreams`**: Add a new LLM provider (URL, token, certificates).
*   **`DELETE /api/admin/downstreams/{id}`**: Remove a provider.

---

## 📊 3. Observability & Stats

### `GET /api/admin/stats`
Returns high-level system metrics.
```json
{
  "totalRequests": 1425,
  "totalPatterns": 42,
  "totalDownstreams": 3
}
```

### `GET /api/admin/audit-logs`
Fetches paginated records of every system interaction.
*   **Query Params**: `page` (default 0), `size` (default 50).

---

## 🚦 4. System Health & Monitoring

*   **`GET /actuator/health`**: Liveness and readiness probes.
*   **`GET /actuator/prometheus`**: Real-time metrics for scrape jobs.
*   **`GET /swagger-ui.html`**: Interactive documentation (Swagger UI).

---
*Developed by **Dhoondlay Engineering** for high-security enterprise environments.*
