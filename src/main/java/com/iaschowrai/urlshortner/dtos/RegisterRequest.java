package com.iaschowrai.urlshortner.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public class RegisterRequest {

    private String username;
    private String email;
    private String password;

    private Set<String> role; // Optional roles, defaults to "ROLE_USER" in service

    // Default constructor
    public RegisterRequest() {
    }

    // Parameterized constructor
    public RegisterRequest(String username, String email, Set<String> role, String password) {
        this.username = username;
        this.email = email;
        this.role = role;
        this.password = password;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<String> getRole() {
        return role;
    }

    public void setRole(Set<String> role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // toString() method for debugging
    @Override
    public String toString() {
        return "RegisterRequest{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", password='[PROTECTED]'" +
                '}';
    }
}
