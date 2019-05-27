package com.isssr.ticketing_system.enumeration;

public enum UserRole {
    ADMIN,
    CUSTOMER,
    HELP_DESK_OPERATOR,
    TEAM_MEMBER,
    TEAM_COORDINATOR,
    TEAM_LEADER;

    public static boolean validateRole(String roleStr){

        for(UserRole role : UserRole.values()){
            if(roleStr.equals(role.toString()))
                return true;
        }
        return false;

    }
}
