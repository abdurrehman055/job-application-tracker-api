package com.abdurrehman.jobtracker.dto.response;

public record AuthResponse(String token, String tokenType) {

    public AuthResponse(String token) {
        this(token, "Bearer");
    }
}
