package io.dhoondlay.shield.detector;

import io.dhoondlay.shield.config.ShieldProperties;
import io.dhoondlay.shield.model.DetectionMatch;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detects API credentials, access tokens, and secrets using regex patterns.
 *
 * <p>Patterns recognised out-of-the-box (all configurable via {@code shield.detectors.credential}):
 * <ul>
 *   <li>HTTP Bearer tokens</li>
 *   <li>AWS Access Keys ({@code AKIA…})</li>
 *   <li>OpenAI API keys ({@code sk-…})</li>
 *   <li>Stripe live/test keys</li>
 *   <li>GitHub personal access tokens ({@code ghp_…})</li>
 *   <li>Google API keys ({@code AIza…})</li>
 *   <li>Generic {@code secret=}, {@code token=}, {@code password=} assignments</li>
 * </ul>
 *
 * <p><strong>Thread-safety:</strong> Stateless; safe for concurrent use.
 */
@Component
public class CredentialDetector extends AbstractRegexDetector {

    public CredentialDetector(ShieldProperties props) {
        super(props);
    }

    @Override
    public String name() {
        return "credential";
    }

    @Override
    public List<DetectionMatch> detect(String text) {
        if (!isEnabled() || text == null || text.isBlank()) {
            return List.of();
        }
        return scanWithPatterns(text, config());
    }
}
