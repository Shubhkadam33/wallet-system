package com.techpulse.wallet.dto;



public class RegisterResponse {

    private Long userId;
    private String email;
    private String message;

    public RegisterResponse() {
    }

    public RegisterResponse(Long userId, String email, String message) {
        this.userId = userId;
        this.email = email;
        this.message = message;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getMessage() {
        return message;
    }
}

