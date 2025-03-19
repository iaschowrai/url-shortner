package com.iaschowrai.urlshortner.service;

import com.iaschowrai.urlshortner.dtos.LoginRequest;
import com.iaschowrai.urlshortner.dtos.RegisterRequest;
import com.iaschowrai.urlshortner.exception.UserAlreadyExistsException;
import com.iaschowrai.urlshortner.models.User;
import com.iaschowrai.urlshortner.repository.UserRepository;
import com.iaschowrai.urlshortner.security.JwtAuthenticationResponse;
import com.iaschowrai.urlshortner.security.JwtUtils;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    // Constructor Injection
    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository,
                       AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    public JwtAuthenticationResponse authenticateUser(LoginRequest loginRequest){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),
                        loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String jwt = jwtUtils.generateToken(userDetails);
        return new JwtAuthenticationResponse(jwt);
    }


    @Transactional
    public void registerUser(RegisterRequest registerRequest){
        if(userRepository.existsByUsername(registerRequest.getUsername())){
            throw new UserAlreadyExistsException("Username is already taken");
        }
        if(userRepository.existsByEmail(registerRequest.getEmail())){
            throw new UserAlreadyExistsException("Email is already taken");
        }
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setRole("ROLE_USER");

        userRepository.save(user);

        userRepository.save(user);
    }

    public User findByUsername(String name) {
        return userRepository.findByUsername(name).orElseThrow(
                () -> new UsernameNotFoundException("User not found with username" + name));

    }
}
