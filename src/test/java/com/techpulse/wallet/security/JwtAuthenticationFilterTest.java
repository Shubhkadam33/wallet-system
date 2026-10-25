package com.techpulse.wallet.security;



import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import io.jsonwebtoken.JwtException;

class JwtAuthenticationFilterTest {

    private JwtService jwtService;
    private CustomUserDetailsService userDetailsService;
    private JwtAuthenticationFilter filter;

    private final UserDetails userDetails =
            User.withUsername("shubham.test@example.com")
                    .password("password")
                    .roles("USER")
                    .build();

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        userDetailsService = mock(CustomUserDetailsService.class);
        filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldContinueWhenAuthorizationHeaderIsMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(200, response.getStatus());
        verifyNoInteractions(jwtService, userDetailsService);
    }

    @Test
    void shouldContinueWhenAuthorizationHeaderIsNotBearer() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic abc123");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtService, userDetailsService);
    }

    @Test
    void shouldAuthenticateUserWhenTokenIsValid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(jwtService.extractUsername("valid-token"))
                .thenReturn("shubham.test@example.com");
        when(userDetailsService.loadUserByUsername(
                "shubham.test@example.com"))
                .thenReturn(userDetails);
        when(jwtService.isTokenValid("valid-token", userDetails))
                .thenReturn(true);

        filter.doFilter(request, response, chain);

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertEquals("shubham.test@example.com",
                authentication.getName());
        assertTrue(authentication.isAuthenticated());
        verify(userDetailsService).loadUserByUsername(
                "shubham.test@example.com");
    }

    @Test
    void shouldNotAuthenticateWhenTokenIsInvalid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid-token");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(jwtService.extractUsername("invalid-token"))
                .thenReturn("shubham.test@example.com");
        when(userDetailsService.loadUserByUsername(
                "shubham.test@example.com"))
                .thenReturn(userDetails);
        when(jwtService.isTokenValid("invalid-token", userDetails))
                .thenReturn(false);

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(200, response.getStatus());
    }

    @Test
    void shouldReturnUnauthorizedWhenJwtIsInvalid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer bad-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractUsername("bad-token"))
                .thenThrow(new JwtException("Invalid token"));

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(401, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertTrue(response.getContentAsString()
                .contains("Invalid or expired token"));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldReturnUnauthorizedWhenUserIsNotFound() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-format-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractUsername("valid-format-token"))
                .thenReturn("missing@example.com");
        when(userDetailsService.loadUserByUsername("missing@example.com"))
                .thenThrow(new UsernameNotFoundException("User not found"));

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString()
                .contains("Invalid or expired token"));
    }

    @Test
    void shouldNotReloadUserWhenAlreadyAuthenticated() throws Exception {
        Authentication existingAuthentication =
                new org.springframework.security.authentication
                        .UsernamePasswordAuthenticationToken(
                                userDetails, null,
                                userDetails.getAuthorities());

        SecurityContextHolder.getContext()
                .setAuthentication(existingAuthentication);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractUsername("valid-token"))
                .thenReturn("shubham.test@example.com");

        filter.doFilter(request, response, new MockFilterChain());

        verifyNoInteractions(userDetailsService);
        verify(jwtService, never())
                .isTokenValid(anyString(), any(UserDetails.class));
        assertSame(existingAuthentication,
                SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldContinueWhenExtractedEmailIsNull() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractUsername("valid-token"))
                .thenReturn(null);

        filter.doFilter(request, response, new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void shouldReturnUnauthorizedWhenIllegalArgumentOccurs() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer bad-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractUsername("bad-token"))
                .thenThrow(new IllegalArgumentException("Bad token"));

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString()
                .contains("Invalid or expired token"));
    }
}
