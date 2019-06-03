package com.isssr.ticketing_system.rest;

import com.fasterxml.jackson.annotation.JsonView;
import com.isssr.ticketing_system.controller.ScrumTeamController;
import com.isssr.ticketing_system.controller.UserController;
import com.isssr.ticketing_system.entity.ScrumTeam;
import com.isssr.ticketing_system.entity.User;
import com.isssr.ticketing_system.exception.EntityNotFoundException;
import com.isssr.ticketing_system.exception.InvalidScrumTeamException;
import com.isssr.ticketing_system.logger.aspect.LogOperation;
import com.isssr.ticketing_system.response_entity.JsonViews;
import com.isssr.ticketing_system.validator.ScrumTeamValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;

import static org.springframework.http.HttpStatus.OK;

@Validated
@RestController
@RequestMapping(path = "scrumteams")
public class ScrumTeamRest {
    private ScrumTeamController scrumTeamController;
    private UserController userController;
    private ScrumTeamValidator scrumTeamValidator;

    @Autowired
    public ScrumTeamRest(ScrumTeamController scrumTeamController, UserController userController,
                         ScrumTeamValidator scrumTeamValidator) {
        if(scrumTeamController == null || userController == null || scrumTeamValidator == null)
            System.out.println("null");

        this.scrumTeamController = scrumTeamController;
        this.userController = userController;
        this.scrumTeamValidator = scrumTeamValidator;

        System.out.println("end constructor");
    }

    /**
     * Configura un validator per gli oggetti di tipo Team
     *
     * @param binder binder
     */
    @InitBinder
    public void setupBinder(WebDataBinder binder) {
        System.out.println("setupBinder");

        binder.addValidators(scrumTeamValidator);
    }

    /**
     * Metodo usato per la gestione di una POST che arriva sull'url specificato. A fronte di
     * una richiesta di questo tipo lo scrum team viene inserito nel DB.
     *
     * @param scrumTeam  scrum team che va aggiunto al DB.
     * @return info del team aggiunto al DB + esito della richiesta HTTP.
     * @see com.isssr.ticketing_system.controller.ScrumTeamController
     */
    @JsonView(JsonViews.DetailedScrumTeam.class)
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<ScrumTeam> insertScrumTeam(@Valid @RequestBody ScrumTeam scrumTeam) {
        try {
            System.out.println(("----   inserting in rest "));
            ScrumTeam createdScrumTeam = scrumTeamController.insertScrumTeam(scrumTeam);
            return new ResponseEntity<>(createdScrumTeam, HttpStatus.CREATED);
        } catch (InvalidScrumTeamException e) {
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
    }

    /**
     * Metodo usato per la gestione di una PUT che arriva sull'url specificato. A fronte di
     * una richiesta di questo tipo viene costruito un team che ha id specificato, team leader specificato
     * e la lista dei team members specificati
     *
     * @param id, id dello scrum team
     * @param scrummasterID, id dello scrum master da inserire nel team
     * @param productownerID, id del product owner da inserire nel team
     * @param assistantsList lista di id dei team member da inserire nel team
     * @return info del team aggiunto al DB + esito della richiesta HTTP.
     * @see com.isssr.ticketing_system.controller.ScrumTeamController
     */
    @RequestMapping(path = "{ID}/scrummaster/{scrummasterID}/productowner/{productownerID}/teammembers/{teammembersList}", method = RequestMethod.PUT)
    @ResponseStatus(OK)
    public ScrumTeam buildScrumTeam(@PathVariable Long ID, @PathVariable Long scrumMasterID,
                                    @PathVariable Long productOwnerID, @PathVariable Long[] teamMembersList)
            throws EntityNotFoundException {

        ScrumTeam scrumTeam = scrumTeamController.getScrumTeamById(ID);
        User scrumMaster = userController.getUser(scrumMasterID);
        User productOwner = userController.getUser(productOwnerID);
        scrumTeamController.setScrumMaster(scrumTeam, scrumMaster);
        scrumTeamController.setProductOwner(scrumTeam, productOwner);

        List<User> userTypes = userController.findByIdIn(Arrays.asList(teamMembersList));
        scrumTeamController.addTeamMembersToTeam(scrumTeam, userTypes);

        return scrumTeam;

    }
}
