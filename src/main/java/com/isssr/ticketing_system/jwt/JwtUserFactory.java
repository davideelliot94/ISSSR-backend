package com.isssr.ticketing_system.jwt;

import com.isssr.ticketing_system.acl.Authority;
import com.isssr.ticketing_system.acl.groups.Group;
import com.isssr.ticketing_system.controller.GroupController;
import com.isssr.ticketing_system.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is used to create JwtUser(s)
 */
public final class JwtUserFactory {


    private JwtUserFactory() {

    }

    /**
     * Create a JwtUser from User and GroupController
     *
     * @param user the user
     * @param groupController the group controller
     * @return a new JwtUser
     */
    public static JwtUser create(User user, GroupController groupController) {

        List<Authority> authorities = getAuthoritiesFromUser(user, groupController);

        return new JwtUser(
                user.getUsername(),
                user.getPassword(),
                mapToGrantedAuthorities(authorities),
                !user.isDeleted()
        );

    }

    /**
     * convert a list of authorities to list of GrantedAuthorities
     *
     * @param authorities the list of authorities
     * @return a list of GrantedAuthorities
     */
    private static List<GrantedAuthority> mapToGrantedAuthorities(List<Authority> authorities) {
        return authorities.stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getAuthorityName().name()))
                .collect(Collectors.toList());
    }

    /**
     * Get authorities from a specified user
     *
     * @param user the user
     * @param groupController the group controller
     * @return a list of authorities
     */
    private static List<Authority> getAuthoritiesFromUser(User user, GroupController groupController) {

        List<Group> groups = groupController.getGroupsByMember(user);

        List<Authority> authorities = new ArrayList<>();
        for (Group g : groups) {
            authorities.addAll(g.getGrantedAuthorities());
        }
        return authorities;
    }

}
