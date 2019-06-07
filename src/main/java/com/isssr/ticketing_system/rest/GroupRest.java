package com.isssr.ticketing_system.rest;


import com.isssr.ticketing_system.acl.Authority;
import com.isssr.ticketing_system.acl.groups.Group;
import com.isssr.ticketing_system.controller.UserController;
import com.isssr.ticketing_system.logger.aspect.LogOperation;
import com.isssr.ticketing_system.entity.User;
import com.isssr.ticketing_system.dao.AuthorityDAO;
import com.isssr.ticketing_system.controller.GroupController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "groups", produces = "application/json")
public class GroupRest {

    private final GroupController groupController;
    private final AuthorityDAO authorityDAO;
    private final UserController userController;

    @Autowired
    public GroupRest(GroupController groupController, AuthorityDAO authorityDAO, UserController userController) {
        this.groupController = groupController;
        this.authorityDAO = authorityDAO;
        this.userController = userController;
    }

    @GetMapping
    @ResponseStatus(OK)
    public List<Group> getGroups() {
        return groupController.getGroups();
    }

    @PostMapping
    @ResponseStatus(CREATED)
    public Group saveGroup(@RequestBody Group group) {
        return groupController.saveGroup(group);
    }

    @GetMapping("{ID}")
    @ResponseStatus(OK)
    public Group getGroup(@PathVariable Long ID) {
        return groupController.getGroup(ID);
    }

    @PutMapping("{ID}")
    @ResponseStatus(OK)
    public Group updateGroup(@PathVariable Long ID, @RequestBody Group group) {
        return groupController.updateGroup(ID, group);
    }

    @DeleteMapping("{ID}")
    @ResponseStatus(NO_CONTENT)
    public void deleteGroup(@PathVariable Long ID) {
        groupController.deleteGroup(ID);
    }

    // NOTA BENE
    // usersList e' un array di ID di utenti NON di id di userType
    @PutMapping("{ID}/authorities/{authoritiesList}/users/{usersList}")
    @ResponseStatus(OK)
    @LogOperation(inputArgs = {"ID,authoritiesList,usersList"}, returnObject = false, tag = "build_group", opName = "buildGroup")
    public Group buildGroup(@PathVariable Long ID,
                            @PathVariable Long[] authoritiesList, @PathVariable Long[] usersList) {
        Group group = groupController.getGroup(ID);

        List<Authority> grantedAuthorities =
                authorityDAO.findByIdIn(Arrays.asList(authoritiesList));
        List<User> users = userController.findByIdIn(Arrays.asList(usersList));

        group.setGrantedAuthorities(grantedAuthorities);
        group.setUsers(users);

        groupController.saveGroup(group);
        return group;

    }

    @GetMapping("{ID}/authorities")
    @ResponseStatus(OK)
    public List<Authority> getAuthoritiesByGroup(@PathVariable Long ID) {
        Group g = groupController.getGroup(ID);
        return g.getGrantedAuthorities();
    }

    @GetMapping("{ID}/users")
    @ResponseStatus(OK)
    public List<User> getUsersByGroup(@PathVariable Long ID) {
        Group g = groupController.getGroup(ID);
        List<User> types = g.getMembers();
        List<User> users = new ArrayList<>();
        for (User type : types) {
            users.add(type);
        }
        return users;
    }
}
