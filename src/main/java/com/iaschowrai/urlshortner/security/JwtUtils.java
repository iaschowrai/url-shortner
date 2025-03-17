package com.iaschowrai.urlshortner.security;

import com.iaschowrai.urlshortner.service.UserDetailsImpl;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

/*
The `JwtUtils` class handles JSON Web Token (JWT) operations, including token generation, validation, and extraction
of user details. It generates tokens with a subject (username) and roles, signs them using a secret key, and sets an
expiration time. It also provides methods to extract the JWT from an HTTP request header and retrieve the username
from a given token. This utility is essential for implementing authentication in a Spring Boot application using
JWT-based security.
 */

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String jwtSecret;
    @Value("${jwt.expiration}")
    private int jwtExpirationMS;

    // Extract JWT from the Authorization header
    public String getJwtFromHeader(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");
        if(bearerToken != null && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }
        return null;
    }

    // Generate a JWT token for a user
    public String generateToken(UserDetailsImpl userDetails){
        String username = userDetails.getUsername();
        String roles = userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.joining(","));
        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date((new Date().getTime() + jwtExpirationMS)))
                .signWith(key())
                .compact();
    }

    // Get username from the JWT token
    public String getUserNameFromJwtToken(String token){
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // Generate the signing key from the base64 decoded JWT secret
    public Key key(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // Validate JWT token's integrity and expiration
    public boolean validateToken(String authToken){
        try {
            Jwts.parser()
                    .verifyWith((SecretKey) key())
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (JwtException e) {
            // Logging can be added here for better debugging
            throw new JwtException("Invalid or expired JWT token.", e);
        } catch (Exception e) {
            // Log and handle other exceptions
            throw new RuntimeException("Unexpected error during token validation.", e);
        }
    }


}
