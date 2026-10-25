package com.techpulse.wallet.controller;



import com.techpulse.wallet.dto.AuthResponse;
import com.techpulse.wallet.dto.LoginRequest;
import com.techpulse.wallet.dto.RegisterRequest;
import com.techpulse.wallet.dto.RegisterResponse;
import com.techpulse.wallet.service.AuthService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void registerShouldReturnCreatedStatus() {
        RegisterRequest request = new RegisterRequest();

        RegisterResponse expected = new RegisterResponse(
                1L,
                "test@example.com",
                "Registration successful"
        );

        when(authService.register(request)).thenReturn(expected);

        ResponseEntity<RegisterResponse> response =
                authController.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(expected, response.getBody());
        verify(authService).register(request);
    }

    @Test
    void loginShouldReturnOkStatus() {
        LoginRequest request = new LoginRequest();

        AuthResponse expected = new AuthResponse(
                "test-jwt-token",
                "test@example.com"
        );

        when(authService.login(request)).thenReturn(expected);

        ResponseEntity<AuthResponse> response =
                authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());
        verify(authService).login(request);
    }
}

