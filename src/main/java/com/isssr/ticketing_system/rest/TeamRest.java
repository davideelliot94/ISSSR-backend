package com.isssr.ticketing_system.rest;

import com.fasterxml.jackson.annotation.JsonView;
import com.isssr.ticketing_system.controller.UserController;
import com.isssr.ticketing_system.entity.SoftDelete.SoftDelete;
import com.isssr.ticketing_system.entity.SoftDelete.SoftDeleteKind;
import com.isssr.ticketing_system.entity.User;
import com.isssr.ticketing_system.exception.EntityNotFoundException;
import com.isssr.ticketing_system.entity.Team;
import com.isssr.ticketing_system.logger.aspect.LogOperation;
import com.isssr.ticketing_system.response_entity.CommonResponseEntity;
import com.isssr.ticketing_system.response_entity.JsonViews;
import com.isssr.ticketing_system.controller.TeamController;
import com.isssr.ticketing_system.response_entity.ResponseEntityBuilder;
import com.isssr.ticketing_system.validator.TeamValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

import static org.springframework.http.HttpStatus.OK;

/**
 * Questa classe gestisce le richieste HTTP che giungono sul path specificato ("team")
 * attraverso i metodi definiti nella classe TeamController.
 */
@Validated
@RestController
@RequestMapping(path = "teams")
@CrossOrigin("*")
@SoftDelete(SoftDeleteKind.NOT_DELETED)
public class TeamRest {

    private TeamController teamController;
    private UserController userController;
    private TeamValidator teamValidator;

    @Autowired
    public TeamRest(
            TeamController teamController,
            UserController userController,
            TeamValidator teamValidator
    ) {
        this.teamController = teamController;
        this.userController = userController;
        this.teamValidator = teamValidator;
    }


    /**
     * Configura un validator per gli oggetti di tipo Team
     *
     * @param binder binder
     */
    @InitBinder
    public void setupBinder(WebDataBinder binder) {
        binder.addValidators(teamValidator);
    }


    /**
     * Metodo usato per la gestione di una POST che arriva sull'url specificato. A fronte di
     * una richiesta di questo tipo il team viene inserito nel DB.
     *
     * @param team team che va aggiunto al DB.
     * @return info del team aggiunto al DB + esito della richiesta HTTP.
     * @see com.isssr.ticketing_system.controller.TeamController
     */
    @JsonView(JsonViews.DetailedTeam.class)
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Team> insertTeam(@Valid @RequestBody Team team) {
        Team createdTeam = teamController.insertTeam(team);
        return new ResponseEntity<>(createdTeam, HttpStatus.CREATED);
    }



    /**
     * Metodo usato per la gestione di una GET che arriva sull'url specificato. A fronte di
     * una richiesta di questo tipo viene restituito il team che ha l'id specificato.
     *
     * @return team con id specificato + esito della richiesta HTTP.
     * @see com.isssr.ticketing_system.controller.TeamController
     */
    @JsonView(JsonViews.DetailedTeam.class)
    @RequestMapping(path = "{id}", method = RequestMethod.GET)
    public ResponseEntity<Team> get(@PathVariable Long id) {
        Team team = null;
        try {
            team = teamController.getTeamById(id);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(team, HttpStatus.OK);
    }

    /**
     * Metodo usato per la gestione di una PUT che arriva sull'url specificato. A fronte di
     * una richiesta di questo tipo il team indicato viene aggiornato nel DB con le info specificate.
     *
     * @param id Id del team che va aggiornato.
     * @param team Info aggiornate del team.
     * @return team eventualmente aggiornato + esito della richiesta HTTP.
     * @see com.isssr.ticketing_system.controller.TeamController
     */
    @JsonView(JsonViews.DetailedTeam.class)
    @RequestMapping(path = "{id}", method = RequestMethod.PUT)
    public ResponseEntity<Team> updateTeamById(@PathVariable Long id, @Valid @RequestBody Team team) {
        Team updatedTeam;
        try {
            updatedTeam = teamController.updateTeamById(id, team);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(updatedTeam, HttpStatus.OK);
    }

    /**
     * Metodo usato per la gestione di una DELETE che arriva sull'url specificato. A fronte di
     * una richiesta di questo tipo il team specificato viene cancellato dal DB.
     *
     * @param id Id del team che va cancellato dal DB.
     * @return info del team cancellato dal DB + esito della richiesta HTTP.
     * @see com.isssr.ticketing_system.controller.TeamController
     */
    @JsonView(JsonViews.DetailedTeam.class)
    @RequestMapping(path = "{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Boolean> deleteTeamById(@PathVariable Long id) {
        boolean deleted = false;
        try {
            deleted = teamController.deleteTeamById(id);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(deleted, HttpStatus.OK);
    }

    /**
     * Metodo usato per la gestione di una GET che arriva sull'url specificato. A fronte di
     * una richiesta di questo tipo vengono restituiti tutti i team presenti nel DB.
     *
     * @return team presenti nel DB + esito della richiesta HTTP.
     * @see com.isssr.ticketing_system.controller.TeamController
     */
    @JsonView(JsonViews.DetailedTeam.class)
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<List<Team>> getAllTeams() {
        List<Team> teams = teamController.getAllTeams();
        return new ResponseEntity<>(teams, HttpStatus.OK);
    }


    /**
     * Metodo usato per la gestione di una DELETE che arriva sull'url specificato. A fronte di
     * una richiesta di questo tipo tutti i team vengono eliminati dal sistema.
     *
     * @return esito della richiesta HTTP.
     * @see com.isssr.ticketing_system.controller.TeamController
     */
    @JsonView(JsonViews.DetailedTeam.class)
    @RequestMapping(method = RequestMethod.DELETE)
    public ResponseEntity<Team> deleteAll() {
        Long count = teamController.count();

        if (count == 0)
            return CommonResponseEntity.NotFoundResponseEntity("TEAMS_NOT_FOUND");

        teamController.deleteAll();

        return CommonResponseEntity.OkResponseEntity("DELETED");
    }


    /**
     * Ricerca di tutti i TeamMember di un Team
     *
     * @param id Id del team di cui interessano i TeamMember
     * @return Lista dei TeamMember del Team
     */
    @RequestMapping(path = "{id}/assistants", method = RequestMethod.GET)
    @ResponseStatus(OK)
    public ResponseEntity<List<User>> getTeamMembersByTeamId(@PathVariable Long id) throws EntityNotFoundException {
        Team team = teamController.getTeamById(id);
        List<User> members = new ArrayList<User>(team.getTeamMembers());

        return new ResponseEntity<>(members, HttpStatus.OK);
    }

    /**
     * Ricerca il TeamLeader di un Team
     *
     * @param id ID del team di cui interessa il TeamLeader
     * @return Lista dei TeamMember del Team
     */
    @RequestMapping(path = "/team_leader/{id}", method = RequestMethod.GET)
    public ResponseEntity<User> getTeamLeaderByTeamId(@PathVariable Long id) {
        User teamLeader = teamController.getTeamLeaderByTeamId(id);
        return new ResponseEntity<>(teamLeader, HttpStatus.OK);
    }

    /**
     * Ricerca di tutti i TeamMember assistenti di un TeamLeader
     *
     * @param id ID del TeamLeader di cui servono gli assistenti
     * @return Lista dei TeamMember assistenti del TeamLeader
     */
    @RequestMapping(path = "/team_member/team_leader/{id}", method = RequestMethod.GET)
    public ResponseEntity<Collection<User>> getTeamMemberByTeamLeaderId(@PathVariable Long id) {
        Collection<User> listTeamMember = teamController.getTeamMembersByTeamLeaderId(id);
        if(listTeamMember != null)
            return new ResponseEntity<>(listTeamMember, HttpStatus.OK);
        else
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }


    /**
     * Metodo usato per la gestione di una PUT che arriva sull'url specificato. A fronte di
     * una richiesta di questo tipo viene costruito un team che ha id specificato, team leader specificato
     * e la lista dei team members specificati
     *
     * @param id id del team da aggiornare
     * @param leaderID id del team leader da inserire nel team
     * @param assistantsList lista di id dei team member da inserire nel team
     * @return info del team aggiunto al DB + esito della richiesta HTTP.
     * @see com.isssr.ticketing_system.controller.TeamController
     */
    @RequestMapping(path = "{ID}/leader/{leaderID}/assistants/{assistantsList}", method = RequestMethod.PUT)
    @ResponseStatus(OK)
    @LogOperation(inputArgs = {"ID,leaderID,assistantsList"}, returnObject = false, tag = "build_team", opName = "buildTeam")
    public Team buildTeam(@PathVariable Long ID,
                          @PathVariable Long leaderID, @PathVariable Long[] assistantsList) throws EntityNotFoundException {

        Team team = teamController.getTeamById(ID);
        User leader = userController.getUser(leaderID);
        teamController.setTeamLeader(team, leader);

        List<User> userTypes = userController.findByIdIn(Arrays.asList(assistantsList));
        teamController.addAssistantsToTeam(team, userTypes);

        return team;

    }

    /**
     * Aggiunta di un TeamMember ad un Team
     *
     * @param teamID ID del Team destinazione.
     * @param teamMemberID Id del  TeamMember da Aggiungere al Team
     * @return l'oggetto TeamMember aggiornato con l 'aggiunta del Team
     */
    @RequestMapping(path = "/add_team_member/{teamID}/{teamMemberID}", method = RequestMethod.PUT)
    public ResponseEntity<Team> addTeamMember(@PathVariable("teamID") Long teamID, @PathVariable("teamMemberID") Long teamMemberID){
        Team updatedTeam;
        try {
            updatedTeam = teamController.addTeamMember(teamID,teamMemberID);
        } catch(EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(updatedTeam, HttpStatus.OK);
    }


    /**
     * Metodo che ritorna tutti i team associati ad una determinata persona
     *
     * @param person username del team leader
     * @return lista di team associati al team leader specificato
     */
    @RequestMapping(path = "findAllTeamsByPerson/{person}", method = RequestMethod.GET)
    public ResponseEntity findAllTeamsByPerson(@PathVariable String person) {
        List<Team> teams;
        try {
            teams = teamController.findAllTeamByPerson(person);
        } catch (EntityNotFoundException e) {
            return CommonResponseEntity.NotFoundResponseEntity("USER_NOT_FOUND");
        }
        return new ResponseEntityBuilder<>(teams).setStatus(HttpStatus.OK).build();
    }
}
