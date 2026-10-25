package com.techpulse.wallet.service;

import com.techpulse.wallet.dto.LoginRequest;
import com.techpulse.wallet.dto.AuthResponse;
import org.springframework.security.authentication.BadCredentialsException;

import com.techpulse.wallet.dto.RegisterRequest;
import com.techpulse.wallet.dto.RegisterResponse;
import com.techpulse.wallet.entity.User;
import com.techpulse.wallet.entity.Wallet;
import com.techpulse.wallet.enums.Role;
import com.techpulse.wallet.repository.UserRepository;
import com.techpulse.wallet.repository.WalletRepository;
import com.techpulse.wallet.security.JwtService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerShouldCreateUserAndWallet() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("  TEST@example.com  ");
        request.setPassword("TestPass123");

        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("TestPass123"))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(1L);
                    return user;
                });

        RegisterResponse response = authService.register(request);

        assertEquals("test@example.com", response.getEmail());

        verify(userRepository).save(argThat(user ->
                user.getEmail().equals("test@example.com")
                && user.getPassword().equals("encoded-password")
                && user.getRole() == Role.USER));

        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void registerShouldRejectDuplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("TestPass123");

        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(request));

        assertEquals("Email is already registered", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
        verify(walletRepository, never()).save(any(Wallet.class));
    }

private LoginRequest createLoginRequest(String email, String password) {
    LoginRequest request = new LoginRequest();
    request.setEmail(email);
    request.setPassword(password);
    return request;
}

private User createTestUser() {
    User user = new User();
    user.setId(1L);
    user.setEmail("test@example.com");
    user.setPassword("encoded-password");
    user.setRole(Role.USER);
    return user;
}

@Test
void loginShouldReturnTokenForValidCredentials() {
    LoginRequest request =
            createLoginRequest(" TEST@example.com ", "TestPass123");

    User user = createTestUser();

    when(userRepository.findByEmail("test@example.com"))
            .thenReturn(java.util.Optional.of(user));
    when(passwordEncoder.matches("TestPass123", "encoded-password"))
            .thenReturn(true);
    when(jwtService.generateToken(any(
            org.springframework.security.core.userdetails.UserDetails.class)))
            .thenReturn("test-jwt-token");

    AuthResponse response = authService.login(request);

    assertEquals("test@example.com", response.getEmail());
    assertEquals("test-jwt-token", response.getToken());
    assertEquals("Bearer", response.getTokenType());
}

@Test
void loginShouldRejectUnknownEmail() {
    LoginRequest request =
            createLoginRequest("unknown@example.com", "TestPass123");

    when(userRepository.findByEmail("unknown@example.com"))
            .thenReturn(java.util.Optional.empty());

    assertThrows(BadCredentialsException.class,
            () -> authService.login(request));

    verify(passwordEncoder, never()).matches(any(), any());
    verify(jwtService, never()).generateToken(any());
}

@Test
void loginShouldRejectIncorrectPassword() {
    LoginRequest request =
            createLoginRequest("test@example.com", "WrongPass123");

    User user = createTestUser();

    when(userRepository.findByEmail("test@example.com"))
            .thenReturn(java.util.Optional.of(user));
    when(passwordEncoder.matches("WrongPass123", "encoded-password"))
            .thenReturn(false);

    assertThrows(BadCredentialsException.class,
            () -> authService.login(request));

    verify(jwtService, never()).generateToken(any());
}

}

