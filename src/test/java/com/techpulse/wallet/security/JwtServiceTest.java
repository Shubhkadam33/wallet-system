package com.techpulse.wallet.security;



import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.Base64;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    // Base64-encoded 32-byte key for tests only
    private final String testSecret = Base64.getEncoder()
            .encodeToString(
                    "0123456789abcdef0123456789abcdef"
                            .getBytes());

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtService();

        // Inject the secret into the private field
        Field secretField = JwtService.class
                .getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(jwtService, testSecret);

        userDetails = User.withUsername("shubham.test@example.com")
                .password("password")
                .roles("USER")
                .build();
    }

    @Test
    void generateTokenShouldCreateTokenWithCorrectUsername() {
        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals(
                "shubham.test@example.com",
                jwtService.extractUsername(token));
    }

    @Test
    void extractUsernameShouldReturnUsernameFromToken() {
        String token = jwtService.generateToken(userDetails);

        String username = jwtService.extractUsername(token);

        assertEquals("shubham.test@example.com", username);
    }

    @Test
    void isTokenValidShouldReturnTrueForCorrectUser() {
        String token = jwtService.generateToken(userDetails);

        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValidShouldReturnFalseForDifferentUser() {
        String token = jwtService.generateToken(userDetails);

        UserDetails anotherUser = User.withUsername("other@example.com")
                .password("password")
                .roles("USER")
                .build();

        assertFalse(jwtService.isTokenValid(token, anotherUser));
    }

    @Test
    void generateTokenShouldContainIssuedAtAndExpiration() {
        String token = jwtService.generateToken(userDetails);

        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(
                        Base64.getDecoder().decode(testSecret)))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(new Date()));
    }
}

