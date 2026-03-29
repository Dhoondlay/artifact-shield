package io.dhoondlay.shield.model;

/**
 * Immutable representation of a single sensitive datum found by a detector.
 *
 * @param detectorName  The name of the detector (e.g. "credential", "pii").
 * @param patternName   Pattern that matched (e.g. "AWS_ACCESS_KEY").
 * @param start         Start offset (inclusive) in the original input.
 * @param end           End offset (exclusive) in the original input.
 * @param matchedValue  The actual substring that was matched.
 * @param riskWeight    Risk points contributed by this single match.
 * @param placeholder   The replacement token written into the sanitised output.
 */
public record DetectionMatch(
        String detectorName,
        String patternName,
        int    start,
        int    end,
        String matchedValue,
        int    riskWeight,
        String placeholder
) {
    public String label() {
        return detectorName.toUpperCase() + " / " + patternName;
    }
}
