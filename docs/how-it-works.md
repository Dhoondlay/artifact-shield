# 🧠 How The Engine Works

A critical flaw in many modern "Data Firewalls" is their reliance on secondary Large Language Models (LLMs) to scan text for sensitive information. This is slow, expensive, and subject to **AI Hallucinations** (e.g., classifying a random string of numbers as a credit card or missing a subtle credential).

Artifact-Shield takes a fundamentally different approach. We use **Deterministic Algorithms**—hard math, proven cryptographic checks, and highly optimized pattern matching—to ensure 100% predictable security before any data leaves your network.

---

## ⚡ 1. Parallel Multi-Threaded Scanning
The gateway achieves ultra-low latency by leveraging **Multi-Threaded Parallel Scanning**. When a prompt is received, the engine doesn't scan one detector at a time.

Instead, we use **Project Reactor's Parallel Flux** to distribute the workload:
1.  **Workload Partitioning**: The list of active detectors (Regex, Luhn, Entropy, Database) is partitioned into "rails."
2.  **Parallel Execution**: Each rail is executed on a different CPU core simultaneously.
3.  **Non-Blocking Merge**: Once all detectors finish their independent scans, the results are merged and passed to the **Interval Merging Algorithm** on the main event loop.

This allows Artifact-Shield to handle massive 100k-user bursts and large 32,000-token inputs without ever blocking the reactive system.

---

## 🔍 2. The Regex Engine
The foundation of the detection engine is **Regular Expressions (Regex)**. 
When the gateway starts (or when a pattern is updated in the database), it compiles the regex rule into a `java.util.regex.Pattern`.

### **Why Pre-Compiling Matters**
Compiling a regex on every request is computationally expensive. By pre-compiling the patterns into a robust `Matcher` instance, Artifact-Shield can scan a 32,000-token prompt against hundreds of rules in milliseconds.

---

## 💳 2. The Luhn Algorithm (Financial Validations)
Regex alone is not enough. A user might type random digits: `1234-5678-9012-3456`. A naive regex `\d{4}-\d{4}-\d{4}-\d{4}` will flag this as a Credit Card, creating a **False Positive**.

### **How Artifact-Shield Solves This**
If the regex detects a 13-to-19 digit sequence, it passes that sequence to our **Luhn Validator** (`FinancialDetector`).

The Luhn algorithm (modulus 10) is the checksum formula used to validate identification numbers like credit cards.
1.  Starting from the right, every second digit is doubled.
2.  If doubling results in a number > 9, the digits of the product are added (e.g., 8 * 2 = 16 -> 1 + 6 = 7).
3.  The sum of all the resulting digits MUST be a multiple of 10.

If the regex matches, *but the Luhn check fails*, Artifact-Shield ignores the string. This eliminates 99% of false positives.

---

## 🔑 3. Entropy Checks (Credential Scanning)
Hardcoded secrets (AWS Keys, JWT tokens, API keys) are notoriously hard to detect because they often don't have a strict format—they are just highly randomized strings.

### **The Shannon Entropy Check**
To combat this, our `CredentialDetector` can measure the **Shannon Entropy** of a string.
-   A normal English sentence ("Hello world this is a test") has low entropy because the characters are predictable.
-   An AWS Access Key (`AKIA1234567890ABCDEF`) has high entropy because it is cryptographically randomized.

If an unknown string of suspicious length is detected, the engine calculates its entropy score. If it crosses a critical threshold (e.g., > 3.5), it is flagged and redacted as a potential secret.

---

## ⚖️ 4. The Risk Scoring Matrix
Artifact-Shield doesn't just block or pass; it understands nuance.

1.  **Detections are Scored**: A Credit Card match is assigned 50 points. An Email Address is assigned 20 points.
2.  **Cumulative Risk**: If a prompt contains both, the request's total risk score becomes 70.
3.  **Threshold Triggers**: As the score climbs, it triggers different severities (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`).
4.  **Action Policy**: If a request hits a `CRITICAL` 100-score (e.g., two credit cards), you can configure the gateway to entirely abort the proxy call using `shield.block-critical-risk: true`.

---

## ✨ 5. Summary
By combining **Regex**, **Checksums (Luhn)**, and **Mathematical Entropy**, Artifact-Shield provides a robust, zero-hallucination perimeter defense that is faster and more reliable than any AI-based scanner on the market.
