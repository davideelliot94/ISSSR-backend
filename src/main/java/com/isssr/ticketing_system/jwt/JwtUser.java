package com.isssr.ticketing_system.jwt;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * This class contains informations for an authenticated user
 */
public class JwtUser implements UserDetails {

    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;


    public JwtUser(String username, String password, Collection<? extends GrantedAuthority> authorities, boolean enabled) {
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        //this.enabled = enabled;
        this.enabled = true;
    }

    /**
     * Get authorities of the user
     *
     * @return a collection containing the authorities
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    /**
     * Get password of the user
     *
     * @return the password
     */
    @JsonIgnore
    @Override
    public String getPassword() {
        return this.password;
    }

    /**
     * Get the username of the user
     *
     * @return the username
     */
    @Override
    public String getUsername() {
        return this.username;
    }

    /**
     * Check if the user is not expired
     *
     * @return true if not expired, false otherwise
     */
    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * [UNUSED] Check if the account is not locked
     *
     * @return true if the account is not locked, false otherwise
     */
    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Check if user credentials are not expired
     *
     * @return true if user credentials are not expired, false otherwise
     */
    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Check if the user is enabled
     *
     * @return true if enabled, false otherwise
     */
    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}
