package io.dhoondlay.shield.detector;

import org.springframework.stereotype.Component;

/**
 * Pure, stateless implementation of the <strong>Luhn Algorithm</strong> (ISO/IEC 7812).
 *
 * <p>The algorithm works as follows for a digit-only string:
 * <ol>
 *   <li>Starting from the rightmost digit (check digit), double every second digit
 *       moving left.</li>
 *   <li>If doubling produces a value {@literal >} 9, subtract 9.</li>
 *   <li>Sum all digits. A valid card number produces a sum divisible by 10.</li>
 * </ol>
 *
 * <p>This implementation runs in <strong>O(n)</strong> time with zero heap allocations,
 * making it suitable for hot-path, high-throughput use.
 *
 * <p><strong>Thread-safety:</strong> All methods are pure functions; safe for concurrent use.
 */
@Component
public class LuhnValidator {

    /**
     * Validates a digit-only string against the Luhn checksum.
     *
     * @param digits a string containing only {@code [0-9]}; spaces and dashes must be
     *               stripped by the caller before invoking this method.
     * @return {@code true} if the string is a valid Luhn number; {@code false} otherwise.
     */
    public boolean isValid(String digits) {
        if (digits == null || digits.length() < 13 || digits.length() > 19) {
            return false;
        }

        int sum    = 0;
        int length = digits.length();
        boolean alternate = false;

        for (int i = length - 1; i >= 0; i--) {
            char c = digits.charAt(i);
            if (c < '0' || c > '9') return false;   // non-digit guard

            int n = c - '0';
            if (alternate) {
                n *= 2;
                if (n > 9) n -= 9;
            }
            sum += n;
            alternate = !alternate;
        }

        return (sum % 10) == 0;
    }
}
