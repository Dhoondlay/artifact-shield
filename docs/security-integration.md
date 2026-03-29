# 🔐 Enterprise Security: OIDC & OAuth2 Integration

Artifact-Shield supports the **OAuth2/OIDC** standard, allowing you to protect the gateway using your company's existing identity provider (IDP). When enabled, the gateway functions as a **JWT Resource Server**, validating tokens before processing any text.

---

## 🚀 1. Supported Providers
The gateway is built on **Spring Security (WebFlux)**, ensuring out-of-the-box compatibility with any OIDC-compliant provider:
*   **Azure Active Directory (Azure AD)**
*   **Okta**
*   **Keycloak**
*   **Auth0**
*   **Google Identity**

---

## 🛠️ 2. Configuration (`application.yml`)

To secure the gateway, you must enable security and point the gateway to your provider's **Issuer URI**.

```yaml
shield:
  security:
    enabled: true                  # Turn ON JWT protection
    jwt-issuer-uri: "https://your-provider.com/realms/your-realm"
    jwt-jwk-set-uri: "https://your-provider.com/realms/your-realm/protocol/openid-connect/certs"
```

### **Detailed Key Breakdown**
| Key | Purpose |
| :--- | :--- |
| `jwt-issuer-uri` | The root URI of your OIDC provider. Used for discovery and claims validation. |
| `jwt-jwk-set-uri` | The endpoint where Artifact-Shield fetches public keys to verify the signature of incoming JWTs. |

---

## 📦 3. Provider-Specific Setup Examples

### **Scenario A: Microsoft Azure AD**
1.  **Register App**: Create an "App Registration" in Azure Portal.
2.  **Expose API**: Add a scope (e.g., `Shield.Scan`).
3.  **Config**:
    ```yaml
    shield:
      security:
        enabled: true
        jwt-issuer-uri: "https://login.microsoftonline.com/{tenant-id}/v2.0"
    ```

### **Scenario B: Keycloak**
1.  **Create Realm**: e.g., `Enterprise`.
2.  **Create Client**: e.g., `artifact-shield`.
3.  **Config**:
    ```yaml
    shield:
      security:
        enabled: true
        jwt-issuer-uri: "http://keycloak:8080/realms/Enterprise"
    ```

---

## 🕵️ 4. How the Gateway Validates Requests

When `security.enabled` is `true`:
1.  **Intercept**: The gateway blocks all requests to `/v1/shield/**`.
2.  **Extract**: It looks for an `Authorization: Bearer <JWT>` header.
3.  **Verify**: It checks the JWT signature using keys from `jwt-jwk-set-uri`.
4.  **Claims**: It ensures the token is not expired and has a valid issuer.
5.  **Proceed**: Only then does the gateway pass the text to the Redaction Engine.

---

## 🌐 5. CORS for Web Dashboards
If you are hosting a custom UI on a different domain, ensure `shield.security.cors-enabled` is `true`. The gateway will then allow cross-origin requests from your authorized dashboard domain.

---
*For more help, contact your security engineer or visit the [Artifact-Shield internal wiki].*
