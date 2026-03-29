package io.dhoondlay.shield.detector;

import io.dhoondlay.shield.config.ShieldProperties;
import io.dhoondlay.shield.model.DetectionMatch;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Detects valid credit card numbers using a two-phase approach:
 * <ol>
 *   <li><strong>Regex phase</strong> – identifies 15–16-digit sequences (with optional
 *       spaces or dashes).</li>
 *   <li><strong>Luhn phase</strong> – mathematically validates each candidate via
 *       {@link LuhnValidator} to eliminate false positives (e.g. random run of digits).</li>
 * </ol>
 *
 * <p>Luhn validation can be disabled via {@code shield.detectors.financial.options.luhnValidation=false}
 * for environments where speed is more important than precision.
 *
 * <p><strong>Thread-safety:</strong> Stateless; safe for concurrent use.
 */
@Component
public class FinancialDetector extends AbstractRegexDetector {

    private final LuhnValidator luhnValidator;

    public FinancialDetector(ShieldProperties props, LuhnValidator luhnValidator) {
        super(props);
        this.luhnValidator = luhnValidator;
    }

    @Override
    public String name() {
        return "financial";
    }

    @Override
    public List<DetectionMatch> detect(String text) {
        if (!isEnabled() || text == null || text.isBlank()) {
            return List.of();
        }

        // Run base regex scan
        List<DetectionMatch> candidates = scanWithPatterns(text, config());
        boolean luhnEnabled = isLuhnEnabled();

        List<DetectionMatch> confirmed = new ArrayList<>();
        for (DetectionMatch candidate : candidates) {
            if (luhnEnabled) {
                String digits = candidate.matchedValue().replaceAll("[\\s\\-]", "");
                if (luhnValidator.isValid(digits)) {
                    confirmed.add(candidate);
                }
            } else {
                confirmed.add(candidate);
            }
        }
        return confirmed;
    }

    private boolean isLuhnEnabled() {
        String option = config().getOptions().getOrDefault("luhnValidation", "true");
        return Boolean.parseBoolean(option);
    }
}
