package com.isssr.ticketing_system.jwt.service;

import com.isssr.ticketing_system.enumeration.UserRole;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;
import java.util.Collection;

/**
 * This class implements a response for a jwt authentication request
 */
@Getter
@Setter
public class JwtAuthenticationResponse implements Serializable {

    private static final long serialVersionUID = 1250166508152483573L;

    private final String username;
    Collection<? extends GrantedAuthority> authorities;
    private UserRole userRole;
    private Long userId;


    public JwtAuthenticationResponse(
            Long userId,
            String username,
            Collection<? extends GrantedAuthority> authorities,
            UserRole userRole) {
        this.userId = userId;
        this.username = username;
        this.authorities = authorities;
        this.userRole = userRole;
    }


}
