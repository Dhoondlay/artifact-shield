# 🔒 Artifact-Shield Security Scenarios

This document outlines common usage scenarios for the Artifact-Shield gateway, detailing inputs, expected redactions, and final system outputs.

---

## 📅 Scenario 1: Basic Redaction (Redact Only)
**Action**: `R` (Default)  
**Input**: `"My email is john.doe@company.com and my phone is +1 555-0199."`

**System Logic**:
1.  Detects `john.doe@company.com` via **PII Detector**.
2.  Detects `+1 555-0199` via **PII Detector**.
3.  Replaces matches with configured placeholders.

**Expected Output**:
```json
{
  "sanitizedText": "My email is [REDACTED_EMAIL] and my phone is [REDACTED_PHONE_NUMBER].",
  "llmResponse": null,
  "severity": "MEDIUM",
  "riskScore": 40,
  "wasProxied": false
}
```

---

## 🤖 Scenario 2: Secure LLM Proxy (Forwarding)
**Action**: `F` (Forward)  
**Input**: `"Add this secret to my config: sk-live-12345abcdef67890."`  
**Downstream Alias**: `openai-gpt4`

**System Logic**:
1.  Detects `sk-live-12345abcdef67890` via **Credential Detector**.
2.  Assesses risk as `CRITICAL` (100).
3.  Sanitizes input: `"Add this secret to my config: [REDACTED_STRIPE_KEY]."`
4.  Forwards **Sanitized Text** to OpenAI endpoint.
5.  Returns OpenAI's response back to the user.

**Expected Output**:
```json
{
  "sanitizedText": "Add this secret to my config: [REDACTED_STRIPE_KEY].",
  "llmResponse": "I have successfully processed your request to update the config with the redacted key.",
  "severity": "CRITICAL",
  "riskScore": 100,
  "wasProxied": true
}
```

---

## 🕵️ Scenario 3: Audit Only (Analyze)
**Action**: `A` (Audit)  
**Input**: `"Tell me a story about a secret agent named 007."`

**System Logic**:
1.  Scans text and finds no PII.
2.  Risk Score is `0` (`CLEAN`).
3.  Audits the request metadata.
4.  Does **NOT** forward to LLM.

**Expected Output**:
```json
{
  "sanitizedText": "Tell me a story about a secret agent named 007.",
  "llmResponse": null,
  "severity": "CLEAN",
  "riskScore": 0,
  "wasProxied": false
}
```

---

## 💳 Scenario 4: Financial Validation (Luhn)
**Action**: `R`  
**Input**: `"Charge card 4111 1111 1111 1111 please."`

**System Logic**:
1.  Regex matches the pattern `(?:\\d[ \\-]?){15,16}`.
2.  `FinancialDetector` applies the **Luhn Algorithm**.
3.  Validates that `4111111111111111` is a valid credit card.
4.  Applies redaction.

**Expected Output**:
```json
{
  "sanitizedText": "Charge card [REDACTED_CREDIT_CARD] please.",
  "severity": "HIGH",
  "riskScore": 50
}
```

---

## ⚠️ Edge Case: Database Missing
**Scenario**: User requests `forwardTo: "unknown-llm"`.  
**System Logic**:
1.  Scans and sanitizes the input.
2.  Attempts to find `unknown-llm` in `shield_downstream_configs`.
3.  Fails resolution.
4.  Logs error in audit trial.

**Expected Output**:
```json
{
  "sanitizedText": "...",
  "llmResponse": "Error: no active downstream found",
  "wasProxied": false
}
```
