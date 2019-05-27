package com.isssr.ticketing_system.rest;

import com.isssr.ticketing_system.acl.Authority;
import com.isssr.ticketing_system.acl.AuthorityName;
import com.isssr.ticketing_system.acl.Identifiable;
import com.isssr.ticketing_system.acl.entrymanager.DomainACLManager;
import com.isssr.ticketing_system.acl.entrymanager.DomainPermission;
import com.isssr.ticketing_system.acl.groups.Group;
import com.isssr.ticketing_system.controller.UserController;
import com.isssr.ticketing_system.rest.acl.ACLDTO;
import com.isssr.ticketing_system.rest.acl.PermissionDTO;
import com.isssr.ticketing_system.rest.acl.SIDDTO;
import com.isssr.ticketing_system.entity.Target;
import com.isssr.ticketing_system.entity.Team;
import com.isssr.ticketing_system.entity.Ticket;
import com.isssr.ticketing_system.entity.User;
import com.isssr.ticketing_system.dao.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "permissions", produces = "application/json")
public class PermissionRest {

    private final AuthorityDAO authorityDAO;
    private final UserController userController;

    private final TeamDao teamDAO;
    private final GroupDAO groupDAO;
    private final TicketDao ticketDAO;
    private final TargetDao softwareProductDAO;

    private final DomainACLManager domainACLManager;

    @Autowired
    public PermissionRest(AuthorityDAO authorityDAO, UserController userController, TeamDao teamDAO, GroupDAO groupDAO, TicketDao ticketDAO, TargetDao softwareProductDAO, DomainACLManager domainACLManager) {
        this.authorityDAO = authorityDAO;
        this.userController = userController;
        this.teamDAO = teamDAO;
        this.groupDAO = groupDAO;
        this.ticketDAO = ticketDAO;
        this.softwareProductDAO = softwareProductDAO;
        this.domainACLManager = domainACLManager;
    }

    @PostMapping
    public void savePermission(@RequestBody @Valid ACLDTO acldto) {

        Long sidId = acldto.getSiddto().getId();

        Integer principal = acldto.getSiddto().getPrincipal();
        Sid sid;
        if (principal == 0) {
            Authority authority = authorityDAO.getOne(sidId);
            sid = authority.convertToSid();
        } else {
            User user = userController.getUser(sidId);
            sid = new PrincipalSid(user.getUsername());
        }

        Long objectId = acldto.getDomainObjectId();


        Identifiable objectDomain = null;
        Class clazz = null;

        switch (acldto.getDomainObjectType()) {
            case "team":
                objectDomain = teamDAO.getOne(objectId);
                clazz = Team.class;
                break;
            case "ticket":
                objectDomain = ticketDAO.getOne(objectId);
                clazz = Ticket.class;
                break;
            case "product":
                objectDomain = softwareProductDAO.getOne(objectId);
                clazz = Target.class;
                break;
        }



        Permission dp = null;

        List<PermissionDTO> perms = acldto.getPerms();
        for (PermissionDTO p : perms) {
            switch (p.getPermission()) {
                case 'R':
                    dp = DomainPermission.READ;
                    break;
                case 'W':
                    dp = DomainPermission.WRITE;
                    break;
                case 'C':
                    dp = DomainPermission.CREATE;
                    break;
                case 'D':
                    dp = DomainPermission.DELETE;
                    break;
            }

            if (p.isGrant()) {
                domainACLManager.addPermission(clazz, objectDomain.getId(), sid, dp);
            } else {
                domainACLManager.denyPermission(clazz, objectDomain.getId(), sid, dp);
            }
        }

    }

    @GetMapping("team/{teamID}")
    public List<ACLDTO> getPermissionOnTeam(@PathVariable Long teamID) {

        List<ACLDTO> result = new ArrayList<>();

        Map<Sid, List<PermissionDTO>> perms = domainACLManager.getAllPermissionOnDomainModel(Team.class, teamID);

        createList(teamID, result, perms, "team");

        return result;

    }

    @GetMapping("product/{productID}")
    public List<ACLDTO> getPermissionOnSoftwareProduct(@PathVariable Long productID) {

        List<ACLDTO> result = new ArrayList<>();

        Map<Sid, List<PermissionDTO>> perms = domainACLManager.getAllPermissionOnDomainModel(Target.class, productID);

        createList(productID, result, perms, "product");

        return result;

    }

    @GetMapping("group/{groupID}")
    public List<ACLDTO> getPermissionOnGroup(@PathVariable Long groupID) {

        List<ACLDTO> result = new ArrayList<>();

        Map<Sid, List<PermissionDTO>> perms = domainACLManager.getAllPermissionOnDomainModel(Group.class, groupID);

        createList(groupID, result, perms, "group");

        return result;

    }

    private void createList(@PathVariable Long objectId, List<ACLDTO> result, Map<Sid, List<PermissionDTO>> perms, String type) {
        for (Map.Entry<Sid, List<PermissionDTO>> entry : perms.entrySet()) {
            ACLDTO acldto = new ACLDTO();
            acldto.setDomainObjectId(objectId);
            acldto.setDomainObjectType(type);

            Sid key = entry.getKey();
            if (key instanceof GrantedAuthoritySid) {
                GrantedAuthoritySid gas = (GrantedAuthoritySid) entry.getKey();
                Authority sid = authorityDAO.findBySid(AuthorityName.valueOf(gas.getGrantedAuthority()));
                acldto.setSiddto(new SIDDTO(sid.getId(), sid.getPrincipal(), sid.getAuthorityName().name()));
            } else if (key instanceof PrincipalSid) {
                PrincipalSid p = new PrincipalSid(((PrincipalSid) key).getPrincipal());
                acldto.setSiddto(new SIDDTO(0L, 1, p.getPrincipal()));
            }

            List<PermissionDTO> permsdto = entry.getValue();

            acldto.setPerms(permsdto);
            result.add(acldto);
        }
    }
}
