package com.isssr.ticketing_system.rest;

import com.fasterxml.jackson.annotation.JsonView;
import com.isssr.ticketing_system.controller.ScrumTeamController;
import com.isssr.ticketing_system.dto.ScrumAssignmentDto;
import com.isssr.ticketing_system.dto.ScrumTeamDto;
import com.isssr.ticketing_system.dto.UserDto;
import com.isssr.ticketing_system.entity.ScrumTeam;
import com.isssr.ticketing_system.entity.SoftDelete.SoftDelete;
import com.isssr.ticketing_system.entity.SoftDelete.SoftDeleteKind;
import com.isssr.ticketing_system.entity.User;
import com.isssr.ticketing_system.exception.EntityNotFoundException;
import com.isssr.ticketing_system.exception.UndeletableScrumTeamException;
import com.isssr.ticketing_system.response_entity.JsonViews;
import com.isssr.ticketing_system.response_entity.ResponseEntityBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

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

    @Autowired
    private ScrumTeamController scrumTeamController;

    @JsonView(JsonViews.DetailedScrumTeam.class)
    @RequestMapping(path = "getScrumTeamList", method = RequestMethod.GET)
    public ResponseEntity<List<ScrumTeam>> getScrumTeamList() {
        List<ScrumTeam> scrumTeams;
        try {
            scrumTeams = scrumTeamController.getScrumTeamList();
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntityBuilder<>(scrumTeams).setStatus(HttpStatus.OK).build();
    }

    @JsonView(JsonViews.DetailedScrumTeam.class)
    @RequestMapping(path = "getProductOwnerBySTId/{id}", method = RequestMethod.GET)
    public User getProductOwnerBySTId(@PathVariable Long id) {

        return scrumTeamController.getProductOwnerBySTId(id);

    }

    @JsonView(JsonViews.DetailedScrumTeam.class)
    @RequestMapping(path = "getScrumMasterBySTId/{id}", method = RequestMethod.GET)
    public User getScrumMasterBySTId(@PathVariable Long id) {

        return scrumTeamController.getScrumMasterBySTId(id);

    }

    @JsonView(JsonViews.DetailedScrumTeam.class)
    @RequestMapping(path = "getMembersBySTId/{id}", method = RequestMethod.GET)
    public ArrayList<User> getMembersBySTId(@PathVariable Long id) {

        return scrumTeamController.getMembersBySTId(id);


    }

    @JsonView(JsonViews.DetailedScrumTeam.class)
    @RequestMapping(path = "assignProduct/{scrumTeamId}/{productId}/{workflowId}", method = RequestMethod.POST)
    public ResponseEntity<ScrumAssignmentDto> assignProduct(@PathVariable Long scrumTeamId,
                                                            @PathVariable Long productId, @PathVariable Long workflowId) {

        ScrumAssignmentDto scrumAssignmentDto = scrumTeamController.assignProduct(scrumTeamId, productId, workflowId);
        return new ResponseEntityBuilder<>(scrumAssignmentDto).setStatus(HttpStatus.OK).build();
    }

    /**
     * Metodo usato per la gestione di una POST che arriva sull'url specificato. A fronte di
     * una richiesta di questo tipo lo scrum team viene inserito nel DB.
     *
     * @param scrumTeam  scrum team che va aggiunto al DB.
     * @return info del team aggiunto al DB + esito della richiesta HTTP.
     * @see com.isssr.ticketing_system.controller.ScrumTeamController
     */
    @RequestMapping(path = "/", method = RequestMethod.POST)

    public ResponseEntity<ScrumTeamDto> insertScrumTeam(@RequestBody ScrumTeamDto scrumTeam, @AuthenticationPrincipal Principal principal) {
        try {
            ScrumTeamDto createdScrumTeam = scrumTeamController.insertScrumTeam(scrumTeam);
            return new ResponseEntityBuilder<ScrumTeamDto>(createdScrumTeam).setStatus(HttpStatus.CREATED).build();
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /*Metodo usato per trovare lo scrum team che lavora ad uno Sprint*/
    @RequestMapping(path= "/sprint/{sprintId}", method = RequestMethod.GET)
    public ResponseEntity<List<UserDto>> getSprintScrumTeam(@PathVariable Long sprintId) {
        List<UserDto> foundTeam = null;
        try {
            foundTeam = scrumTeamController.findTeamBySprint(sprintId);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntityBuilder<>(foundTeam).setStatus(HttpStatus.OK).build();
    }

    @RequestMapping(path = "/{scrumTeamId}",  method = RequestMethod.DELETE)
    public ResponseEntity deleteScrumTeam(@PathVariable Long scrumTeamId) {
        try {
            scrumTeamController.deleteScrumTeam(scrumTeamId);
            return new ResponseEntity(HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        } catch (UndeletableScrumTeamException e) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

    }
}
