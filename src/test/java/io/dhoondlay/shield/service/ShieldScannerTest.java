package io.dhoondlay.shield.service;

import io.dhoondlay.shield.detector.Detector;
import io.dhoondlay.shield.model.DetectionMatch;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@DisplayName("ShieldScanner tests")
class ShieldScannerTest {

    @Autowired
    private ShieldScanner scanner;

    @MockBean(name = "credentialDetector")
    private Detector detector;

    @Test
    @DisplayName("Calls enabled detector and collects matches (Parallel)")
    void callsEnabledDetector() {
        when(detector.isEnabled()).thenReturn(true);
        when(detector.detect(anyString())).thenReturn(List.of(
            new DetectionMatch("test", "A", 0, 5, "hello", 1, "[R]")
        ));

        List<DetectionMatch> matches = scanner.scanParallel("hello world").block();

        assertThat(matches).isNotEmpty();
        assertThat(matches.get(0).patternName()).isEqualTo("A");
    }

    @Test
    @DisplayName("Skips disabled detector")
    void skipsDisabledDetector() {
        when(detector.isEnabled()).thenReturn(false);
        List<DetectionMatch> matches = scanner.scanParallel("test").block();
        assertThat(matches).isEmpty();
    }

    @Test
    @DisplayName("Handles detector failures gracefully")
    void handlesDetectorFailure() {
        when(detector.isEnabled()).thenReturn(true);
        when(detector.detect(anyString())).thenThrow(new RuntimeException("Oops"));

        List<DetectionMatch> matches = scanner.scanParallel("test").block();
        assertThat(matches).isEmpty(); // Logged but didn't crash
    }
}
