package com.iaschowrai.urlshortner.controller;


import com.iaschowrai.urlshortner.dtos.LoginRequest;
import com.iaschowrai.urlshortner.dtos.RegisterRequest;
import com.iaschowrai.urlshortner.models.User;
import com.iaschowrai.urlshortner.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }


    @PostMapping("/public/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest){
        return ResponseEntity.ok(userService.authenticateUser(loginRequest));
    }


    @PostMapping("/public/register")
    public ResponseEntity<?> registerUser(@Valid  @RequestBody RegisterRequest registerRequest){
        userService.registerUser(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }
}
