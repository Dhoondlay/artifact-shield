package io.dhoondlay.shield.api;

import io.dhoondlay.shield.api.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("GlobalExceptionHandler tests")
class GlobalExceptionHandlerTest {

    @Autowired
    private GlobalExceptionHandler exceptionHandler;

    @Test
    @DisplayName("IllegalArgumentException returns 400")
    void handleIllegalArgument() {
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgument(new IllegalArgumentException("invalid"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.error()).isEqualTo("BAD_REQUEST");
    }

    @Test
    @DisplayName("ConstraintViolationException returns 400")
    void handleConstraintViolation() {
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleConstraintViolation(new ConstraintViolationException("violated", null));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.error()).isEqualTo("CONSTRAINT_VIOLATION");
    }

    @Test
    @DisplayName("Unknown Exception returns 500")
    void handleUnknown() {
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleUnknown(new RuntimeException("oops"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.error()).isEqualTo("INTERNAL_ERROR");
    }
}
