package com.isssr.ticketing_system.jwt;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * This class implements a jwt authentication request object for a login request
 */
@Getter
@Setter
public class JwtAuthenticationRequest implements Serializable {

    private static final long serialVersionUID = -8445943548965154778L;

    private String username;
    private String password;

    public JwtAuthenticationRequest() {
        super();
    }

    public JwtAuthenticationRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public String toString() {
        return "JwtAuthenticationRequest{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
