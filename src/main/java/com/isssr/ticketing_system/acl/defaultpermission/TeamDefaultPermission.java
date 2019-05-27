package com.isssr.ticketing_system.acl.defaultpermission;

import com.isssr.ticketing_system.acl.AuthorityName;
import com.isssr.ticketing_system.acl.entrymanager.DomainPermission;
import com.isssr.ticketing_system.entity.Team;
import lombok.Getter;
import org.springframework.security.acls.model.Permission;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

@Component
public class TeamDefaultPermission extends DefaultPermissionTable {


    public TeamDefaultPermission() {
        super(Team.class);
    }

    protected void populateMaps() {
        List<Permission> permissionsCoordinator = new ArrayList<>();
        permissionsCoordinator.add(DomainPermission.READ);
        permissionsCoordinator.add(DomainPermission.WRITE);
        permissionsCoordinator.add(DomainPermission.DELETE);
        permissionsCoordinator.add(DomainPermission.CREATE);

        this.grantedMap.put(AuthorityName.ROLE_TEAM_COORDINATOR, permissionsCoordinator);

        List<Permission> permissionsReader = new ArrayList<>();
        permissionsReader.add(DomainPermission.READ);
        this.grantedMap.put(AuthorityName.ROLE_TEAM_READER, permissionsReader);

        List<Permission> permissionsDeniedReader = new ArrayList<>();
        permissionsDeniedReader.add(DomainPermission.WRITE);
        permissionsDeniedReader.add(DomainPermission.CREATE);
        permissionsDeniedReader.add(DomainPermission.DELETE);
        this.deniedMap.put(AuthorityName.ROLE_TEAM_READER, permissionsDeniedReader);

    }


}
