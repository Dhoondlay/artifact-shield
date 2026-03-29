# 🔌 Client Integration Guide

This guide provides drop-in code examples for integrating the **Artifact-Shield** gateway into your own applications.

---

## ☕ 1. Java / Spring Boot (WebClient)
The recommended way for Spring-based microservices to call the reactive gateway.

```java
@Service
public class ShieldClient {

    private final WebClient webClient;

    public ShieldClient(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("http://shield-gateway:8080").build();
    }

    public Mono<SanitizeResponse> callAi(String userPrompt) {
        return webClient.post()
                .uri("/v1/shield/sanitize")
                .bodyValue(Map.of(
                    "content", userPrompt,
                    "action", "FORWARD",
                    "forwardTo", "gemini"
                ))
                .retrieve()
                .bodyToMono(SanitizeResponse.class);
    }
}
```

---

## 🐍 2. Python (Requests)
Standard synchronous integration for Python applications.

```python
import requests

def call_shield(content, provider="openai"):
    url = "http://shield-gateway:8080/v1/shield/sanitize"
    payload = {
        "content": content,
        "action": "FORWARD",
        "forwardTo": provider
    }
    
    response = requests.post(url, json=payload)
    response.raise_for_status()
    
    return response.json()

# Usage
data = call_shield("My SSN is 000-00-0000")
print(f"Sanitized Prompt: {data['sanitizedText']}")
print(f"LLM Response: {data['llmResponse']}")
```

---

## 🟢 3. Node.js (Axios)
Common integration for Express or NestJS backends.

```javascript
const axios = require('axios');

async function callShield(text) {
  try {
    const response = await axios.post('http://shield-gateway:8080/v1/shield/sanitize', {
      content: text,
      action: 'FORWARD',
      forwardTo: 'gemini'
    });
    
    return response.data;
  } catch (error) {
    console.error('Shield call failed:', error.message);
    throw error;
  }
}
```

---

## 📜 4. Enterprise Best Practices

1.  **Timeouts**: Always set a timeout (e.g., 60s) on your client call, as LLM responses can take significantly longer than standard REST calls.
2.  **Auth Headers**: If you enabled OIDC/OAuth2 on the gateway, ensure you pass the `Authorization: Bearer <JWT>` header in every request.
3.  **Error Handling**: Handle `413 Payload Too Large` if your prompt exceeds the configured `shield.max-input-length`.
4.  **Correlation IDs**: If your ecosystem uses Distributed Tracing (Zipkin/Jaeger), ensure you pass the `X-Correlation-ID` header to link the gateway's logs with your application's logs.

---
*Developed by **Dhoondlay Engineering** for high-security enterprise environments.*
