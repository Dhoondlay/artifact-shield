package io.dhoondlay.shield.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Externally configurable properties for Artifact-Shield.
 *
 * <p>All regex patterns, risk weights, enabled/disabled flags, and placeholder
 * templates are driven from {@code application.yml} (prefix: {@code shield}).
 * This allows operators to tune detection without touching source code.
 *
 * <pre>
 * shield:
 *   detectors:
 *     credential:
 *       enabled: true
 *       ...
 * </pre>
 */
@Component
@ConfigurationProperties(prefix = "shield")
public class ShieldProperties {

    /** Global on/off switch for the entire redaction pipeline. */
    private boolean enabled = true;

    /** Maximum input length (characters) accepted by the API. */
    private int maxInputLength = 32_000;

    /** 
     * Whether to block forwarding if risk reaches CRITICAL (100).
     * Defaults to false (sanitize and forward).
     */
    private boolean blockCriticalRisk = false;

    /** Detector-level configuration, keyed by detector name. */
    private Map<String, DetectorConfig> detectors = defaultDetectors();

    /** Security-score thresholds (0-100) mapped to severity labels. */
    private ScoreThresholds scoreThresholds = new ScoreThresholds();

    /** Security & OAuth2 settings. */
    private SecuritySettings security = new SecuritySettings();

    // -------------------------------------------------------------------------
    // Nested configuration types
    // -------------------------------------------------------------------------

    public static class DetectorConfig {
        /** Whether this detector participates in the pipeline. */
        private boolean enabled = true;

        /**
         * Risk weight contributed to the score for each match found.
         * Score = min(100, sum of all weights * match counts).
         */
        private int weightPerMatch = 20;

        /**
         * Named regex patterns used by the detector.
         * Key = pattern name (used in placeholder generation), value = regex string.
         */
        private Map<String, String> patterns = new LinkedHashMap<>();

        /** Placeholder template. {@code {type}} is replaced with the pattern name. */
        private String placeholderTemplate = "[REDACTED_{type}]";

        /** Additional detector-specific flags (open-ended extensibility). */
        private Map<String, String> options = new LinkedHashMap<>();

        // Getters & setters
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getWeightPerMatch() { return weightPerMatch; }
        public void setWeightPerMatch(int weightPerMatch) { this.weightPerMatch = weightPerMatch; }
        public Map<String, String> getPatterns() { return patterns; }
        public void setPatterns(Map<String, String> patterns) { this.patterns = patterns; }
        public String getPlaceholderTemplate() { return placeholderTemplate; }
        public void setPlaceholderTemplate(String placeholderTemplate) { this.placeholderTemplate = placeholderTemplate; }
        public Map<String, String> getOptions() { return options; }
        public void setOptions(Map<String, String> options) { this.options = options; }
    }

    public static class ScoreThresholds {
        private int low    = 20;
        private int medium = 50;
        private int high   = 75;

        public int getLow()    { return low;    }
        public void setLow(int low)    { this.low = low;    }
        public int getMedium() { return medium; }
        public void setMedium(int medium) { this.medium = medium; }
        public int getHigh()   { return high;   }
        public void setHigh(int high)   { this.high = high;   }
    }

    public static class SecuritySettings {
        private boolean enabled = false;
        private String jwtIssuerUri;
        private String jwtJwkSetUri;
        private boolean corsEnabled = true;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getJwtIssuerUri() { return jwtIssuerUri; }
        public void setJwtIssuerUri(String jwtIssuerUri) { this.jwtIssuerUri = jwtIssuerUri; }
        public String getJwtJwkSetUri() { return jwtJwkSetUri; }
        public void setJwtJwkSetUri(String jwtJwkSetUri) { this.jwtJwkSetUri = jwtJwkSetUri; }
        public boolean isCorsEnabled() { return corsEnabled; }
        public void setCorsEnabled(boolean corsEnabled) { this.corsEnabled = corsEnabled; }
    }

    // -------------------------------------------------------------------------
    // Default detector configuration (applied when nothing is in YAML)
    // -------------------------------------------------------------------------

    private static Map<String, DetectorConfig> defaultDetectors() {
        Map<String, DetectorConfig> map = new LinkedHashMap<>();

        // ---- Credential detector ----
        DetectorConfig cred = new DetectorConfig();
        cred.setWeightPerMatch(35);
        cred.getPatterns().put("BEARER_TOKEN",
                "(?i)Bearer\\s+[A-Za-z0-9\\-._~+/]+=*");
        cred.getPatterns().put("AWS_ACCESS_KEY",
                "\\bAKIA[0-9A-Z]{16}\\b");
        cred.getPatterns().put("OPENAI_KEY",
                "\\bsk-[A-Za-z0-9]{32,}\\b");
        cred.getPatterns().put("STRIPE_KEY",
                "\\b(sk|pk)_(live|test)_[A-Za-z0-9]{24,}\\b");
        cred.getPatterns().put("GITHUB_TOKEN",
                "\\bghp_[A-Za-z0-9]{36}\\b");
        cred.getPatterns().put("GOOGLE_API_KEY",
                "\\bAIza[A-Za-z0-9\\-_]{35}\\b");
        cred.getPatterns().put("GENERIC_SECRET",
                "(?i)(secret|token|apikey|api_key|passwd|password)\\s*[=:]\\s*['\"]?[A-Za-z0-9\\-_]{12,}['\"]?");
        map.put("credential", cred);

        // ---- Financial detector ----
        DetectorConfig fin = new DetectorConfig();
        fin.setWeightPerMatch(50);
        fin.getPatterns().put("CREDIT_CARD",
                "\\b(?:\\d[ \\-]?){15,16}\\b");   // raw; Luhn validation applied in code
        fin.getOptions().put("luhnValidation", "true");
        map.put("financial", fin);

        // ---- PII detector ----
        DetectorConfig pii = new DetectorConfig();
        pii.setWeightPerMatch(20);
        pii.getPatterns().put("EMAIL",
                "[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}");
        pii.getPatterns().put("IPV4",
                "\\b(?:(?:25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(?:25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\b");
        pii.getPatterns().put("IPV6",
                "(?:[A-Fa-f0-9]{1,4}:){7}[A-Fa-f0-9]{1,4}|"
                        + "(?:[A-Fa-f0-9]{1,4}:){1,7}:|"
                        + "(?:[A-Fa-f0-9]{1,4}:){1,6}:[A-Fa-f0-9]{1,4}|"
                        + ":(?::[A-Fa-f0-9]{1,4}){1,7}|::");
        pii.getPatterns().put("PHONE_NUMBER",
                // Require either leading '+' OR at least one separator (space, hyphen, dot)
                // between digit groups to avoid matching raw numeric sequences (card numbers, IDs)
                "(?:\\+[\\d\\s\\-.]{7,18}\\d|\\b\\d{2,4}[\\s\\-.]+\\d{2,4}(?:[\\s\\-.]+\\d{2,4}){1,3}\\b)");
        pii.getPatterns().put("SSN",
                "\\b(?!000|666|9\\d{2})\\d{3}-(?!00)\\d{2}-(?!0000)\\d{4}\\b");
        map.put("pii", pii);

        return map;
    }

    // -------------------------------------------------------------------------
    // Root-level getters & setters
    // -------------------------------------------------------------------------

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public int getMaxInputLength() { return maxInputLength; }
    public void setMaxInputLength(int maxInputLength) { this.maxInputLength = maxInputLength; }
    public boolean isBlockCriticalRisk() { return blockCriticalRisk; }
    public void setBlockCriticalRisk(boolean blockCriticalRisk) { this.blockCriticalRisk = blockCriticalRisk; }
    public Map<String, DetectorConfig> getDetectors() { return detectors; }
    public void setDetectors(Map<String, DetectorConfig> detectors) { this.detectors = detectors; }
    public ScoreThresholds getScoreThresholds() { return scoreThresholds; }
    public void setScoreThresholds(ScoreThresholds scoreThresholds) { this.scoreThresholds = scoreThresholds; }
    public SecuritySettings getSecurity() { return security; }
    public void setSecurity(SecuritySettings security) { this.security = security; }
}
