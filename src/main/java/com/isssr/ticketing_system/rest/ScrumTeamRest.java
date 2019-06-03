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
import java.util.ArrayList;
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
        this.scrumTeamController = scrumTeamController;
        this.userController = userController;
        this.scrumTeamValidator = scrumTeamValidator;
    }

    /**
     * Configura un validator per gli oggetti di tipo Team
     *
     * @param binder binder
     */
    @InitBinder
    public void setupBinder(WebDataBinder binder) {
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
            ScrumTeam createdScrumTeam = scrumTeamController.insertScrumTeam(scrumTeam);
            return new ResponseEntity<>(createdScrumTeam, HttpStatus.CREATED);
        } catch (InvalidScrumTeamException e) {
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
    }

    /**
     * Metodo usato per la gestione di una GET che arriva sull'url specificato. A fronte di
     * una richiesta di questo tipo viene restituito il team che ha l'id specificato.
     *
     * @return team con id specificato + esito della richiesta HTTP.
     * @see com.isssr.ticketing_system.controller.ScrumTeamController
     */
    @JsonView(JsonViews.DetailedScrumTeam.class)
    @RequestMapping(path = "{id}", method = RequestMethod.GET)
    public ResponseEntity<ScrumTeam> get(@PathVariable Long id) {
        ScrumTeam scrumTeam = null;
        try {
            scrumTeam = scrumTeamController.getScrumTeamById(id);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(scrumTeam, HttpStatus.OK);
    }

    /**
     * Ricerca di tutti i TeamMember di un Team
     *
     * @param id Id del team di cui interessano i TeamMember
     * @return Lista dei TeamMember del Team
     */
    @RequestMapping(path = "{id}/teammembers", method = RequestMethod.GET)
    @ResponseStatus(OK)
    public ResponseEntity<List<User>> getTeamMembersByTeamId(@PathVariable Long id) throws EntityNotFoundException {
        ScrumTeam scrumTeam = scrumTeamController.getScrumTeamById(id);
        List<User> members = new ArrayList<User>(scrumTeam.getTeamMembers());

        return new ResponseEntity<>(members, HttpStatus.OK);
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
