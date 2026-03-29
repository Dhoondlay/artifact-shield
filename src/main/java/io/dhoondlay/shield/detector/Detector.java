package io.dhoondlay.shield.detector;

import io.dhoondlay.shield.model.DetectionMatch;
import java.util.List;

/**
 * Strategy interface for identifying sensitive information in text.
 */
public interface Detector {

    /** Identifier for logging and reporting. */
    String name();

    /** Execute the detection logic. */
    List<DetectionMatch> detect(String input);

    /** If false, this detector is skipped. */
    default boolean isEnabled() {
        return true;
    }
}
