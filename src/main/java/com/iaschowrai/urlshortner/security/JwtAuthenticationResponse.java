package com.iaschowrai.urlshortner.security;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;


public class JwtAuthenticationResponse {
    private String token;

    public JwtAuthenticationResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
