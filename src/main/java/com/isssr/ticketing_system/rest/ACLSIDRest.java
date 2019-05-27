package com.isssr.ticketing_system.rest;

import com.isssr.ticketing_system.acl.Authority;
import com.isssr.ticketing_system.controller.AuthorityController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.OK;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "acl-sid", produces = "application/json")
public class ACLSIDRest {

    private final AuthorityController authorityController;

    @Autowired
    public ACLSIDRest(AuthorityController authorityController) {
        this.authorityController = authorityController;
    }

    @GetMapping
    @ResponseStatus(OK)
    public List<Authority> getAllSid() {
        return authorityController.getAllAuthorities();
    }

}
