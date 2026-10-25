package com.techpulse.wallet.dto;



public class AuthResponse {

    private String token;
    private String tokenType;
    private String email;

    public AuthResponse(String token, String email) {
        this.token = token;
        this.email = email;
        this.tokenType = "Bearer";
    }

    public String getToken() {
        return token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getEmail() {
        return email;
    }
}

