package io.dhoondlay.shield.detector;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("LuhnValidator tests")
class LuhnValidatorTest {

    @Autowired
    private LuhnValidator luhnValidator;

    @Test
    @DisplayName("Valid Visa test card returns true")
    void validVisaCardReturnsTrue() {
        assertThat(luhnValidator.isValid("4111111111111111")).isTrue();
    }

    @Test
    @DisplayName("Invalid Luhn returns false")
    void invalidLuhnReturnsFalse() {
        assertThat(luhnValidator.isValid("4111 1111 1111 1112")).isFalse();
    }

    @Test
    @DisplayName("Shorter numbers return false")
    void shortNumbersReturnFalse() {
        assertThat(luhnValidator.isValid("1234 5678")).isFalse();
    }

    @Test
    @DisplayName("Blank or non-numeric returns false")
    void blankReturnsFalse() {
        assertThat(luhnValidator.isValid("test card number")).isFalse();
        assertThat(luhnValidator.isValid("")).isFalse();
        assertThat(luhnValidator.isValid(null)).isFalse();
    }
}
