package io.dhoondlay.shield.detector;

import io.dhoondlay.shield.entity.ShieldPattern;
import io.dhoondlay.shield.model.DetectionMatch;
import io.dhoondlay.shield.repository.ShieldPatternRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A highly dynamic detector that reads patterns directly from the database.
 */
@Component
@RequiredArgsConstructor
public class DynamicDatabaseDetector implements Detector {

    private final ShieldPatternRepository repository;
    private final Map<String, Pattern> patternCache = new ConcurrentHashMap<>();

    @Override
    public String name() {
        return "DYNAMIC_DB_SCANNER";
    }

    @Override
    public List<DetectionMatch> detect(String input) {
        List<DetectionMatch> matches = new ArrayList<>();
        List<ShieldPattern> activePatterns = repository.findByEnabledTrue();

        for (ShieldPattern config : activePatterns) {
            Pattern p = patternCache.computeIfAbsent(config.getRegex(), Pattern::compile);
            Matcher m = p.matcher(input);

            while (m.find()) {
                matches.add(new DetectionMatch(
                        config.getDetectorName(),
                        config.getPatternName(),
                        m.start(),
                        m.end(),
                        m.group(),
                        config.getRiskWeight(),
                        config.getPlaceholderTemplate().replace("{type}", config.getPatternName())
                ));
            }
        }
        return matches;
    }

    public void invalidateCache() {
        patternCache.clear();
    }
}
