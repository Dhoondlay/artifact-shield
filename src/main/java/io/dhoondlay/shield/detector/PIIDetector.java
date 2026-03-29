package io.dhoondlay.shield.detector;

import io.dhoondlay.shield.config.ShieldProperties;
import io.dhoondlay.shield.model.DetectionMatch;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detects Personally Identifiable Information (PII) using deterministic regex patterns.
 *
 * <p>Recognised categories (all configurable via {@code shield.detectors.pii}):
 * <ul>
 *   <li><strong>EMAIL</strong> – standard RFC-5321–compatible address pattern</li>
 *   <li><strong>IPV4</strong> – dotted-decimal notation with valid octet ranges</li>
 *   <li><strong>IPV6</strong> – full and compressed IPv6 notation</li>
 *   <li><strong>PHONE_NUMBER</strong> – E.164-style and common locale formats</li>
 *   <li><strong>SSN</strong> – U.S. Social Security Numbers (excludes invalid ranges)</li>
 * </ul>
 *
 * <p><strong>Thread-safety:</strong> Stateless; safe for concurrent use.
 */
@Component
public class PIIDetector extends AbstractRegexDetector {

    public PIIDetector(ShieldProperties props) {
        super(props);
    }

    @Override
    public String name() {
        return "pii";
    }

    @Override
    public List<DetectionMatch> detect(String text) {
        if (!isEnabled() || text == null || text.isBlank()) {
            return List.of();
        }
        return scanWithPatterns(text, config());
    }
}
