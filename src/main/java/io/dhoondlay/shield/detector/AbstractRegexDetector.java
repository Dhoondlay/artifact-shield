package io.dhoondlay.shield.detector;

import io.dhoondlay.shield.config.ShieldProperties;
import io.dhoondlay.shield.config.ShieldProperties.DetectorConfig;
import io.dhoondlay.shield.model.DetectionMatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstract base class providing common regex-scan behaviour.
 *
 * <p>Subclasses call {@link #scanWithPatterns(String, DetectorConfig)} to iterate
 * all named patterns and collect matches. Pattern objects are compiled once and
 * cached in a {@link ConcurrentHashMap} for zero GC pressure under load.
 */
public abstract class AbstractRegexDetector implements Detector {

    /** Thread-safe pattern cache: pattern-string → compiled {@link Pattern}. */
    private final Map<String, Pattern> patternCache = new ConcurrentHashMap<>();

    protected final ShieldProperties props;

    protected AbstractRegexDetector(ShieldProperties props) {
        this.props = props;
    }

    /** Retrieve the configuration block for this detector. */
    protected DetectorConfig config() {
        return props.getDetectors().getOrDefault(name(), new DetectorConfig());
    }

    @Override
    public boolean isEnabled() {
        return config().isEnabled();
    }

    /**
     * Iterates all named patterns in {@code config} and builds a {@link DetectionMatch}
     * for every non-overlapping regex match found in {@code text}.
     */
    protected List<DetectionMatch> scanWithPatterns(String text, DetectorConfig config) {
        List<DetectionMatch> matches = new ArrayList<>();

        for (Map.Entry<String, String> entry : config.getPatterns().entrySet()) {
            String patternName = entry.getKey();
            String regex       = entry.getValue();

            Pattern pattern = patternCache.computeIfAbsent(
                    name() + ":" + patternName,
                    k -> Pattern.compile(regex)
            );

            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                String placeholder = config.getPlaceholderTemplate()
                        .replace("{type}", patternName);

                matches.add(new DetectionMatch(
                        name(),
                        patternName,
                        matcher.start(),
                        matcher.end(),
                        matcher.group(),
                        config.getWeightPerMatch(),
                        placeholder
                ));
            }
        }

        return matches;
    }
}
