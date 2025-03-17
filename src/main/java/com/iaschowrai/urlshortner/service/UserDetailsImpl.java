package com.iaschowrai.urlshortner.service;

import com.iaschowrai.urlshortner.models.User;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/*
The `UserDetailsImpl` class is a custom implementation of Spring Security's `UserDetails` interface, which is used
for authentication and authorization. It wraps the `User` entity and provides necessary security-related details
like username, password, and roles (authorities). This class is required because Spring Security expects a `UserDetails`
 object when handling authentication, enabling role-based access control. By implementing `UserDetails`, we ensure
 seamless integration with Spring Security, allowing secure user authentication and authorization in our application.
 */

//@Data
@Getter
@NoArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String email;
    private String password;
    private Collection< ? extends  GrantedAuthority> authorities;

    public UserDetailsImpl(long id, String username, String email, String password, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    public static UserDetailsImpl build(User user){
        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole());
        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(authority)
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;  // Modify this if you want to implement expiration logic
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;  // Modify this if you want to implement locking logic
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // Modify this if you want to implement credential expiration
    }

    @Override
    public boolean isEnabled() {
        return true;  // Modify this if you want to implement an enable/disable mechanism
    }

}
