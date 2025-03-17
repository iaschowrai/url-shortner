package com.iaschowrai.urlshortner.security;


import com.iaschowrai.urlshortner.service.UserDetailsServiceImpl;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/*
The WebSecurityConfig class is a Spring Security configuration that secures your application by setting up
authentication, authorization, password encoding, and JWT-based authentication
 */

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class);

    private final UserDetailsServiceImpl userDetailsService;

    @Autowired // Add this annotation for constructor injection
    public WebSecurityConfig(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * JWT Authentication Filter bean.
     * This filter checks and validates JWT tokens in incoming requests.
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    /**
     * Password Encoder Bean.
     * Configures BCryptPasswordEncoder to hash passwords.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        logger.info("Configuring BCryptPasswordEncoder for password hashing.");
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication Provider bean.
     * Configures DaoAuthenticationProvider to authenticate users with database credentials.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        logger.info("Configuring DaoAuthenticationProvider with UserDetailsService and PasswordEncoder.");
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    /**
     * Security Filter Chain configuration.
     * Configures HTTP security, access rules, and integrates JWT authentication filter.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        logger.info("Configuring HTTP security for the application.");

        // Disabling CSRF for stateless API
        http.csrf(AbstractHttpConfigurer::disable)

        // Configuring authorization rules for specific API endpoints
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll() // Allowing public access to auth endpoints and short URL mappings
                .requestMatchers("/api/urls/**").authenticated() // Restricting /api/urls/** endpoints to authenticated users
                    .requestMatchers( "/{shortUrl}").permitAll() // Allowing public access to short URL mappings
                    .anyRequest().authenticated()); // Enforcing authentication for all other requests

        // Setting the custom authentication provider
        http.authenticationProvider(authenticationProvider());

        // Adding JWT Authentication Filter before the UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        logger.info("Security filter chain configured successfully.");

        return http.build();
    }



}
