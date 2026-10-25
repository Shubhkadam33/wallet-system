package com.techpulse.wallet.exception;



import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    @Test
    void handleIllegalArgumentShouldReturnBadRequest() {
        IllegalArgumentException exception =
                new IllegalArgumentException("Insufficient wallet balance");

        ResponseEntity<Map<String, String>> response =
                handler.handleIllegalArgument(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(
                "Insufficient wallet balance",
                response.getBody().get("message")
        );
    }
}
