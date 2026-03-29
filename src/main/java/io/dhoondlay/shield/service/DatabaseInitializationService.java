package io.dhoondlay.shield.service;

import io.dhoondlay.shield.entity.DownstreamConfig;
import io.dhoondlay.shield.entity.ShieldPattern;
import io.dhoondlay.shield.repository.DownstreamRepository;
import io.dhoondlay.shield.repository.ShieldPatternRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Seeds default security patterns on first startup.
 * Uses count() guard so data is never re-seeded or duplicated on restart.
 * With H2 file-based DB (data/shield_db), data persists across restarts.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializationService {

    private final ShieldPatternRepository patternRepository;
    private final DownstreamRepository    downstreamRepository;

    @PostConstruct
    public void init() {
        seedPatterns();
        seedDefaultDownstream();
    }

    private void seedPatterns() {
        if (patternRepository.count() > 0) {
            log.info("[SHIELD] Database already seeded with {} patterns.", patternRepository.count());
            return;
        }
        log.info("[SHIELD] Seeding default security patterns...");

        patternRepository.saveAll(List.of(

            // ── CREDENTIALS ───────────────────────────────────────────────────────
            p("credential", "AWS_ACCESS_KEY",
                "\\bAKIA[0-9A-Z]{16}\\b", 40),
            p("credential", "AWS_SECRET_KEY",
                "(?i)(aws_secret|secret_access_key)\\s*[=:]\\s*[A-Za-z0-9/+]{40}", 40),
            p("credential", "OPENAI_KEY",
                "\\bsk-[A-Za-z0-9]{32,}\\b", 40),
            p("credential", "GITHUB_TOKEN",
                "\\bghp_[A-Za-z0-9]{36}\\b", 40),
            p("credential", "GITHUB_OAUTH",
                "\\bgho_[A-Za-z0-9]{36}\\b", 40),
            p("credential", "STRIPE_SECRET",
                "\\bsk_(live|test)_[A-Za-z0-9]{24,}\\b", 40),
            p("credential", "STRIPE_PUBLISHABLE",
                "\\bpk_(live|test)_[A-Za-z0-9]{24,}\\b", 30),
            p("credential", "GOOGLE_API_KEY",
                "\\bAIza[A-Za-z0-9\\-_]{35}\\b", 40),
            p("credential", "SLACK_TOKEN",
                "\\bxox[baprs]-[A-Za-z0-9\\-]{10,}\\b", 40),
            p("credential", "BEARER_TOKEN",
                "(?i)Bearer\\s+[A-Za-z0-9\\-._~+/]+=*", 35),
            p("credential", "GENERIC_SECRET",
                "(?i)(secret|token|apikey|api_key|passwd|password)\\s*[=:]\\s*['\"]?[A-Za-z0-9\\-_]{12,}['\"]?", 30),
            p("credential", "PRIVATE_KEY_BLOCK",
                "-----BEGIN [A-Z ]+PRIVATE KEY-----", 50),
            p("credential", "TWILIO_TOKEN",
                "\\bsk[a-z0-9]{32}\\b", 35),
            p("credential", "SENDGRID_KEY",
                "\\bSG\\.[A-Za-z0-9\\-_]{22,}\\b", 35),

            // ── FINANCIAL ─────────────────────────────────────────────────────────
            p("financial", "IBAN",
                "\\b[A-Z]{2}\\d{2}[A-Z0-9]{4}\\d{7}([A-Z0-9]?){0,16}\\b", 40),
            p("financial", "SWIFT_BIC",
                "\\b[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?\\b", 25),
            p("financial", "ROUTING_NUMBER",
                "\\b0[0-9]{8}\\b", 30),

            // ── PII ───────────────────────────────────────────────────────────────
            p("pii", "EMAIL",
                "[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}", 25),
            p("pii", "IPV4",
                "\\b(?:(?:25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(?:25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\b", 20),
            p("pii", "IPV6",
                "(?:[A-Fa-f0-9]{1,4}:){7}[A-Fa-f0-9]{1,4}|(?:[A-Fa-f0-9]{1,4}:){1,7}:|:(?::[A-Fa-f0-9]{1,4}){1,7}|::", 20),
            p("pii", "PHONE_NUMBER",
                "(?:\\+[\\d\\s\\-.]{7,18}\\d|\\b\\d{2,4}[\\s\\-.]+\\d{2,4}(?:[\\s\\-.]+\\d{2,4}){1,3}\\b)", 20),
            p("pii", "SSN",
                "\\b(?!000|666|9\\d{2})\\d{3}-(?!00)\\d{2}-(?!0000)\\d{4}\\b", 40),
            p("pii", "PASSPORT",
                "\\b[A-Z]{1,2}[0-9]{6,9}\\b", 35),
            p("pii", "DRIVERS_LICENSE",
                "(?i)(driver.{0,10}license|DL|DLN)\\s*[:#]?\\s*[A-Z0-9]{6,12}", 30),
            p("pii", "DATE_OF_BIRTH",
                "(?i)(dob|date.of.birth|born)\\s*[=:\\-]?\\s*\\d{1,2}[\\-/.\\s]\\d{1,2}[\\-/.\\s]\\d{2,4}", 25),
            p("pii", "NATIONAL_ID",
                "(?i)(national.?id|aadhar|aadhaar|uid)\\s*[=:#]?\\s*\\d{4}[\\s\\-]?\\d{4}[\\s\\-]?\\d{4}", 40),
            p("pii", "PERSON_NAME",
                "(?i)(name|full.name)\\s*[=:]\\s*[A-Z][a-z]+(?:\\s[A-Z][a-z]+)+", 15),

            // ── NETWORK / INFRASTRUCTURE ──────────────────────────────────────────
            p("network", "MAC_ADDRESS",
                "\\b([0-9A-Fa-f]{2}[:\\-]){5}[0-9A-Fa-f]{2}\\b", 20),
            p("network", "INTERNAL_HOST",
                "(?i)(https?://|ssh://)?(?:10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}|192\\.168\\.\\d{1,3}\\.\\d{1,3}|172\\.(?:1[6-9]|2[0-9]|3[0-1])\\.\\d{1,3}\\.\\d{1,3})(:\\d{2,5})?", 25),
            p("network", "PRIVATE_URL",
                "(?i)https?://(?:localhost|127\\.\\d+\\.\\d+\\.\\d+)(:\\d{2,5})?[^\\s]*", 20),
            p("network", "S3_BUCKET_URL",
                "s3://[a-z0-9][a-z0-9\\-]{1,61}[a-z0-9][^\\s]*", 30),
            p("network", "DATABASE_URL",
                "(?i)(jdbc|postgres|mysql|mongodb|redis)://[^\\s]+", 35),

            // ── SECRETS / SSH ─────────────────────────────────────────────────────
            p("secrets", "SSH_DSA_KEY",
                "-----BEGIN DSA PRIVATE KEY-----", 50),
            p("secrets", "SSH_EC_KEY",
                "-----BEGIN EC PRIVATE KEY-----", 50),
            p("secrets", "SSH_RSA_KEY",
                "-----BEGIN RSA PRIVATE KEY-----", 50),
            p("secrets", "ENCRYPTION_KEY",
                "(?i)(encryption.?key|aes.?key|hmac.?secret)\\s*[=:]\\s*[A-Za-z0-9+/=]{16,}", 40),
            p("secrets", "JWT_TOKEN",
                "eyJ[A-Za-z0-9-_]+\\.eyJ[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+", 35)
        ));

        log.info("[SHIELD] Seeded {} default patterns.", patternRepository.count());
    }

    private void seedDefaultDownstream() {
        if (downstreamRepository.count() == 0) {
            downstreamRepository.save(DownstreamConfig.builder()
                .alias("gemini-flash")
                .apiUrl("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent")
                .authType("API_KEY")
                .authToken("YOUR_GEMINI_API_KEY_HERE")
                .promptField("prompt")
                .enabled(false)
                .build());
            log.info("[SHIELD] Created placeholder 'gemini-flash' downstream (disabled). Update the token via Admin API or H2 console.");
        }
    }

    private ShieldPattern p(String detector, String name, String regex, int weight) {
        return ShieldPattern.builder()
                .detectorName(detector)
                .patternName(name)
                .regex(regex)
                .riskWeight(weight)
                .build();
    }
}
