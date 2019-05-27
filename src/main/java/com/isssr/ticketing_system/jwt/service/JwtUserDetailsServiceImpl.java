package com.isssr.ticketing_system.jwt.service;


import com.isssr.ticketing_system.controller.GroupController;
import com.isssr.ticketing_system.dao.UserDao;
import com.isssr.ticketing_system.entity.User;
import com.isssr.ticketing_system.jwt.JwtUserFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * This class implements service methods to handle jwt user details
 */
@Service
@Qualifier(value = "JwtUserDetailsService")
public class JwtUserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserDao userDAO;

    @Autowired
    private GroupController groupController;


    /**
     * Load a user by username from db
     *
     * @param username the username
     * @return a JwtUser containing details of the user
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userDAO.findByUsername(username);

        if (!user.isPresent()) {
            throw new UsernameNotFoundException(String.format("No user found with username '%s'.", username));
        }

        /*
        if (user == null) {
            throw new UsernameNotFoundException(String.format("No user found with username '%s'.", username));
        } else {
            return JwtUserFactory.create(user, groupController);
        }
        */

        return JwtUserFactory.create(user.get(), groupController);
    }

}
