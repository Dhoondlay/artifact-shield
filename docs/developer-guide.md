# 🛠️ Artifact-Shield Developer Guide

This guide describes how to extend the gateway with new detection logic, specifically by adding a custom `Detector`.

---

## 🏗️ How to Add a New Detector (Step-by-Step)

If your organization needs to detect a proprietary or domain-specific string (e.g., Internal Project Codename, Patient ID, etc.), follow these steps:

### 1. Identify the Pattern
Decide on the **Regex** and the **Placeholder** name.
*   **Pattern**: `PRJ-[A-Z0-9]{4,8}`
*   **Placeholder Name**: `PROJECT_ID`

### 2. Create the Detector Class
Create a new class in `io.dhoondlay.shield.detector` and inherit from `AbstractRegexDetector`.

```java
package io.dhoondlay.shield.detector;

import io.dhoondlay.shield.config.ShieldProperties;
import org.springframework.stereotype.Component;

@Component
public class ProjectCodenameDetector extends AbstractRegexDetector {

    public ProjectCodenameDetector(ShieldProperties properties) {
        // "project" is the key in application.yml (shield.detectors.project)
        super(properties, "project");
    }
}
```

### 3. (Optional) Implement Custom Validation
If you need mathematical validation (like Luhn), override the `isValid` method:

```java
@Override
protected boolean isValid(String value, String patternName) {
    if ("PROJECT_ID".equals(patternName)) {
        return value.startsWith("PRJ-") && value.length() > 6;
    }
    return true;
}
```

### 4. Register in `application.yml`
Add your new detector's configuration. You don't need to rebuild if you're only changing the regex!

```yaml
shield:
  detectors:
    project:
      enabled: true
      weightPerMatch: 30
      placeholderTemplate: "[REDACTED_PROJECT]"
      patterns:
        PROJECT_ID: "PRJ-[A-Z0-9]{4,8}"
```

### 5. Verify the Lifecycle
Once the app starts:
1.  `ShieldScanner` (at Line 35) will automatically pick up your new `@Component`.
2.  It will be added to the non-blocking scan execution.
3.  Matches will contribute to the cumulative `riskScore`.

---

## ⚡ Performance Best Practices
*   **Non-Blocking**: Never use `Thread.sleep()` or blocking I/O inside a Detector.
*   **Regex Optimization**: Always use **Non-Backtracking** regex patterns for large inputs.
*   **Memory**: Avoid creating large objects inside the `scan` method.

---
*Gateway Core Team — Security & Privacy Engineering*
