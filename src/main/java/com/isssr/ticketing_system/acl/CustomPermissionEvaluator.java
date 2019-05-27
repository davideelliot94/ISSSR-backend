package com.isssr.ticketing_system.acl;

//import net.petrikainulainen.spring.datasolr.user.dto.SecurityRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author Petri Kainulainen
 */
@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {
    public CustomPermissionEvaluator() {
    }

    public boolean hasPermission(Authentication arg0, Object arg1) {
        System.out.println("CustomPermissionEvaluator.hasPermission");
        System.out.println("arg0 = "+arg0);
        System.out.println("arg1 = "+arg1);

        if (arg0 == null || !arg0.isAuthenticated()) {
            System.out.println("false");
            return false;
        }
        else {
            System.out.println("true");
            for(GrantedAuthority authority: arg0.getAuthorities()) {
                if(authority.getAuthority().equals(arg1))
                    return true;
            }
            return false;
        }
    }

    @Override
    public boolean hasPermission(Authentication arg0, Object arg1, Object arg2) {
        System.out.println("CustomPermissionEvaluator.hasPermission");
        System.out.println("arg0 = "+arg0);
        System.out.println("arg1 = "+arg1);
        System.out.println("arg2 = "+arg2);

        if (arg0 == null || !arg0.isAuthenticated()) {
            System.out.println("false");
            return false;
        }
        else {
            System.out.println("true");
            for(GrantedAuthority authority: arg0.getAuthorities()) {
                if(authority.getAuthority().equals(arg2))
                    return true;
            }
            return false;
        }
    }

    @Override
    public boolean hasPermission(Authentication arg0, Serializable arg1, String arg2, Object arg3) {
        throw new RuntimeException("Id-based permission evaluation not currently supported.");
    }

}