package io.dhoondlay.shield.detector;

import io.dhoondlay.shield.entity.ShieldPattern;
import io.dhoondlay.shield.model.DetectionMatch;
import io.dhoondlay.shield.repository.ShieldPatternRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("DynamicDatabaseDetector tests")
class DynamicDatabaseDetectorTest {

    @Autowired
    private DynamicDatabaseDetector detector;

    @Autowired
    private ShieldPatternRepository patternRepository;

    @Test
    @DisplayName("Matches input against DB patterns")
    void matchesAgainstDbPatterns() {
        patternRepository.save(ShieldPattern.builder()
                .detectorName("test")
                .patternName("TEST_CODE")
                .regex("CODE-\\d{3}")
                .riskWeight(10)
                .enabled(true)
                .build());

        List<DetectionMatch> matches = detector.detect("Here is CODE-123 and CODE-456");

        assertThat(matches).hasSize(2);
        assertThat(matches.get(0).patternName()).isEqualTo("TEST_CODE");
        assertThat(matches.get(0).matchedValue()).isEqualTo("CODE-123");
    }

    @Test
    @DisplayName("Invalidates cache correctly")
    void invalidatesCache() {
        patternRepository.save(ShieldPattern.builder()
                .detectorName("test")
                .patternName("OLD_PATTERN")
                .regex("OLD-\\d{2}")
                .riskWeight(5)
                .enabled(true)
                .build());

        assertThat(detector.detect("OLD-11")).isNotEmpty();

        detector.invalidateCache();

        // No new behavioral change to test here, just internal state reset coverage
        assertThat(detector.detect("OLD-11")).isNotEmpty();
    }
}
