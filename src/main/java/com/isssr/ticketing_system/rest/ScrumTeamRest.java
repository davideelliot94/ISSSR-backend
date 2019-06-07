package com.isssr.ticketing_system.rest;

import com.fasterxml.jackson.annotation.JsonView;
import com.isssr.ticketing_system.controller.ScrumController;
import com.isssr.ticketing_system.entity.ScrumTeam;
import com.isssr.ticketing_system.entity.SoftDelete.SoftDelete;
import com.isssr.ticketing_system.entity.SoftDelete.SoftDeleteKind;
import com.isssr.ticketing_system.entity.User;
import com.isssr.ticketing_system.response_entity.JsonViews;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

/**
 * Questa classe gestisce le richieste HTTP che giungono sul path specificato ("scrumteam")
 * attraverso i metodi definiti nella classe ScrumTeamController.
 */
@Validated
@RestController
@RequestMapping(path = "scrumteam")
@CrossOrigin("*")
@SoftDelete(SoftDeleteKind.NOT_DELETED)
public class ScrumTeamRest {

    private ScrumController scrumController;

    @Autowired
    public ScrumTeamRest(
            ScrumController scrumController
    ) {
        this.scrumController = scrumController;
    }

    @JsonView(JsonViews.DetailedScrumTeam.class)
    @RequestMapping(path = "getScrumTeamList", method = RequestMethod.GET)
    public ArrayList<ScrumTeam> getScrumTeamList() {

        ArrayList<ScrumTeam> scrumTeams = new ArrayList<>();

        scrumTeams =  scrumController.getScrumTeamList();

        return scrumTeams;

    }

    @JsonView(JsonViews.DetailedScrumTeam.class)
    @RequestMapping(path = "getProductOwnerBySTId/{id}", method = RequestMethod.GET)
    public User getProductOwnerBySTId(@PathVariable Long id) {

        return scrumController.getProductOwnerBySTId(id);

    }

    @JsonView(JsonViews.DetailedScrumTeam.class)
    @RequestMapping(path = "getScrumMasterBySTId/{id}", method = RequestMethod.GET)
    public User getScrumMasterBySTId(@PathVariable Long id) {

        return scrumController.getScrumMasterBySTId(id);

    }

    @JsonView(JsonViews.DetailedScrumTeam.class)
    @RequestMapping(path = "getMembersBySTId/{id}", method = RequestMethod.GET)
    public ArrayList<User> getMembersBySTId(@PathVariable Long id) {

        return scrumController.getMembersBySTId(id);


    }

    @JsonView(JsonViews.DetailedScrumTeam.class)
    @RequestMapping(path = "assignProductToST/{tid}/{pid}", method = RequestMethod.POST)
    public void assignProductToST(@PathVariable Long tid, @PathVariable Long pid) {

        System.out.println("entrato");
        scrumController.assignProductToST(tid, pid);

    }
}
