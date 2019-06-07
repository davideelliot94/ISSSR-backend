package com.isssr.ticketing_system.controller;

import com.isssr.ticketing_system.acl.defaultpermission.GroupDefaultPermission;
import com.isssr.ticketing_system.acl.groups.Group;
import com.isssr.ticketing_system.exception.DomainEntityNotFoundException;
import com.isssr.ticketing_system.logger.aspect.LogOperation;
import com.isssr.ticketing_system.entity.User;
import com.isssr.ticketing_system.dao.GroupDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.List;

@Service
public class GroupController {

    @Autowired
    private GroupDAO groupDAO;

    @Autowired
    private GroupDefaultPermission defaultPermissionTable;

    protected boolean verifyExists(Long ID) {
        boolean exists = groupDAO.existsById(ID);
        if (!exists) {
            throw new DomainEntityNotFoundException(ID, Group.class);
        }
        return exists;
    }

    @PostFilter("hasPermission(filterObject,'READ') or hasAuthority('ROLE_ADMIN')")
    public List<Group> getGroups() {
        return groupDAO.findAll();
    }

    @LogOperation(inputArgs = {"g"}, returnObject = true, tag = "insert", opName = "saveGroup")
    @Transactional
    //@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_GROUP_COORDINATOR')")
    public Group saveGroup(Group g) {
        groupDAO.save(g);
        defaultPermissionTable.grantDefaultPermission(g.getId());
        defaultPermissionTable.denyDefaultPermission(g.getId());
        return g;
    }

    @PostAuthorize("hasPermission(returnObject,'READ') or hasAuthority('ROLE_ADMIN')")
    public Group getGroup(@NotNull Long ID) {
        verifyExists(ID);
        return groupDAO.getOne(ID);
    }

    public Group getGroupByName(String name) {
        return groupDAO.findByName(name);
    }

    @Transactional
    //@PreAuthorize("hasPermission(#g,'WRITE') or hasAuthority('ROLE_ADMIN')")
    public Group updateGroup(Long ID, Group g) {
        Group oldGroup = getGroup(ID);
        oldGroup.setName(g.getName());
        return groupDAO.saveAndFlush(oldGroup);
    }

    @Transactional
    @LogOperation(inputArgs = {"ID"}, returnObject = false, tag = "remove", opName = "deleteGroup")
    @PreAuthorize("hasPermission(#ID,'com.uniroma2.isssrbackend.acl.groups.Group','DELETE') or hasAuthority('ROLE_ADMIN')")
    public void deleteGroup(Long ID) {
        verifyExists(ID);
        groupDAO.deleteById(ID);
        defaultPermissionTable.removeDefaultPermission(ID);
    }

    public List<Group> getGroupsByMember(User userType) {
        return groupDAO.findAllByMembersEquals(userType);
    }

    public Long getGroupByRole(String role) {
        return groupDAO.getGroupByRole(role);
    }
}
