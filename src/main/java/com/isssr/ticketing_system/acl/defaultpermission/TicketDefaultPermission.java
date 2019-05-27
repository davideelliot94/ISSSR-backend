package com.isssr.ticketing_system.acl.defaultpermission;

import com.isssr.ticketing_system.acl.AuthorityName;
import com.isssr.ticketing_system.acl.entrymanager.DomainPermission;
import com.isssr.ticketing_system.entity.Ticket;
import lombok.Getter;
import org.springframework.security.acls.model.Permission;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Getter
public class TicketDefaultPermission extends DefaultPermissionTable {


    public TicketDefaultPermission() {
        super(Ticket.class);
    }


    @Override
    protected void populateMaps() {
        List<Permission> permissionsCoordinator = new ArrayList<>();
        permissionsCoordinator.add(DomainPermission.READ);
        permissionsCoordinator.add(DomainPermission.WRITE);
        permissionsCoordinator.add(DomainPermission.CREATE);

        this.grantedMap.put(AuthorityName.ROLE_TEAM_MEMBER, permissionsCoordinator);

        List<Permission> permissionsDeniedReader = new ArrayList<>();
        permissionsDeniedReader.add(DomainPermission.DELETE);
        this.deniedMap.put(AuthorityName.ROLE_TEAM_MEMBER, permissionsDeniedReader);
    }
}
