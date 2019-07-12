package com.isssr.ticketing_system.rest;

import com.fasterxml.jackson.annotation.JsonView;
import com.isssr.ticketing_system.controller.SprintCreateController;
import com.isssr.ticketing_system.controller.TargetController;

import com.isssr.ticketing_system.dto.SprintDTO;
import com.isssr.ticketing_system.dto.SprintWithUserRoleDto;
import com.isssr.ticketing_system.dto.TargetDto;
import com.isssr.ticketing_system.exception.EntityNotFoundException;
import com.isssr.ticketing_system.entity.Sprint;
import com.isssr.ticketing_system.response_entity.CommonResponseEntity;
import com.isssr.ticketing_system.response_entity.JsonViews;
import com.isssr.ticketing_system.response_entity.ResponseEntityBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.NoSuchElementException;

//login REST
//curl 'http://localhost:8200/ticketingsystem/public/login/'  -H 'Content-Type: application/json;charset=utf-8'  --data '{"username":"admin","password":"password"}' -v
//get metadata for sprint insert REST
//curl 'http://localhost:8200/ticketingsystem/sprint/create/1'  -v -H 'Authorization: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImF1ZGllbmNlIjoid2ViIiwicm9sZXMiOlsiUk9MRV9BRE1JTiIsIlJPTEVfVEVBTV9DT09SRElOQVRPUiIsIlJPTEVfR1JPVVBfQ09PUkRJTkFUT1IiLCJST0xFX1NPRlRXQVJFX1BST0RVQ1RfQ09PUkRJTkFUT1IiXSwiaXNFbmFibGVkIjp0cnVlLCJleHAiOjE1NTk1NTgyNzUsImlhdCI6MTU1OTU1MTA3NTQ2NX0.Vj5hXgDO2IEgUijQ3fm6gIzWAzhU8wm36lHA30Qpy38'
//curl 'http://localhost:8200/ticketingsystem/sprint/create/1'  -v -H 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImF1ZGllbmNlIjoid2ViIiwicm9sZXMiOlsiUk9MRV9BRE1JTiIsIlJPTEVfVEVBTV9DT09SRElOQVRPUiIsIlJPTEVfR1JPVVBfQ09PUkRJTkFUT1IiLCJST0xFX1NPRlRXQVJFX1BST0RVQ1RfQ09PUkRJTkFUT1IiXSwiaXNFbmFibGVkIjp0cnVlLCJleHAiOjE1NTk1NzY2NzIsImlhdCI6MTU1OTU2OTQ3MjM3Nn0.3UFLx_OL7eaG-JH4sESHdve7tATwElMqi9HdgRFa5wE'
//post sprint insert REST
//curl 'http://localhost:8200/ticketingsystem/sprint/create/'  -H 'Content-Type: applica"number":-1,"duration":"2","sprintGoal":"null"}' -v  -H 'Authorization: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImF1ZGllbmNlIjoid2ViIiwicm9sZXMiOlsiUk9MRV9BRE1JTiIsIlJPTEVfVEVBTV9DT09SRElOQVRPUiIsIlJPTEVfR1JPVVBfQ09PUkRJTkFUT1IiLCJST0xFX1NPRlRXQVJFX1BST0RVQ1RfQ09PUkRJTkFUT1IiXSwiaXNFbmFibGVkIjp0cnVlLCJleHAiOjE1NTk1NTgyNzUsImlhdCI6MTU1OTU1MTA3NTQ2NX0.Vj5hXgDO2IEgUijQ3fm6gIzWAzhU8wm36lHA30Qpy38'

@Validated
@RestController
@RequestMapping("sprint")
@CrossOrigin("*")
public class SprintRest {


    @Autowired
    private SprintCreateController sprintCreateController;
    @Autowired
    private TargetController targetController;

    @Autowired
    public SprintRest(SprintCreateController sprintCreateController) {
        this.sprintCreateController = sprintCreateController;
    }

    /**
     *  get all metadata infos needed to build sprint insert view
     * @param idProductOwner of the Product Owner
     * @return targets related to Product Owner
     */
    @RequestMapping(path = "create/{idProductOwner}", method = RequestMethod.GET)
    public ResponseEntity getMetadataInsertSprint(@PathVariable Long idProductOwner) {
        List<TargetDto> targets;
        try {
            targets = targetController.getTargetByProductOwnerId(idProductOwner);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(targets, HttpStatus.OK);
    }

    /**
     *  insert a sprint in the system checking possible errors,
     *  access Authorization checked:  FORBIDDEN will be returned if calling user is not ScrumMaster of passed product in the DB
     *                                 also checked if calling user has SCRUM_ROLE to access to this scrum core functionality
     * @param sprintDTO serializzation F.E. side of the sprint to insert
     * @return insert op result
     */

    @RequestMapping(path = "/create", method = RequestMethod.POST)
    public ResponseEntity insertSprint(@RequestBody SprintDTO sprintDTO, @AuthenticationPrincipal Principal principal) {

        try {
            sprintCreateController.insertSprint(sprintDTO,principal.getName());
        } catch (UnauthorizedUserException e1){
            e1.printStackTrace();
            System.err.println("FORBIDDEN TRY ACCESS TO SPRINT INSERT OF "+principal.getName());
            return  new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        catch (IllegalArgumentException e2){
            e2.printStackTrace();
            return  new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        catch (NoSuchElementException e3){
            return  new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        catch (Exception e) {
            e.printStackTrace();
            return CommonResponseEntity.NotFoundResponseEntity("ERRORE NEL INSERIMENTO\n" + e.getMessage(),"sprint");
        }
        return CommonResponseEntity.CreatedResponseEntity("CREATED", "Sprint");
    }

    /**
     * get all sprint of the passed product
     * @param productId of the Product Owner
     * @return sprints related to Product Owner or error
     */
    @JsonView(JsonViews.Basic.class)
    @RequestMapping(path = "product/{productId}/visualize", method = RequestMethod.GET)
    public ResponseEntity getSprintProduct(@PathVariable Long productId) {
        List<SprintDTO> sprints;
        try {
            sprints = sprintCreateController.getAllByProduct(productId);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }catch (Exception e2) {
            e2.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntityBuilder<>(sprints).setStatus(HttpStatus.OK).build();
    }
    /**
     * get all sprint of the passed product owner
     * @param id of the Product Owner
     * @return sprints related to Product Owner
     */
    @JsonView(JsonViews.Basic.class)
    @RequestMapping(path = "productOwner/{id}/visualize", method = RequestMethod.GET)
    public ResponseEntity getSprintProductOwner(@PathVariable Long id) {
        List<SprintDTO> sprints;
        try {
            sprints = sprintCreateController.getSprintsByPO(id);
        }
        catch (NoSuchElementException e1){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        }
        catch (Exception e2){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntityBuilder<>(sprints).setStatus(HttpStatus.OK).build();
    }
    /**
     * get all sprint of the passed ScrumTeamMember
     * @param teamMemberId of the ScrumTeamMember
     * @return sprints related to ScrumTeamMember
     */
    @JsonView(JsonViews.Basic.class)
    @RequestMapping(path = "findByTeamMember/{teamMemberId}", method = RequestMethod.GET)
    public ResponseEntity getSprintsByScrumTeamMember(@PathVariable Long teamMemberId) {
        try {
            List<SprintWithUserRoleDto> sprintsDto = sprintCreateController.getSprintsByScrumTeamMember(teamMemberId);
            return new ResponseEntityBuilder<>(sprintsDto).setStatus(HttpStatus.OK).build();
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    /**
     * Metodo che gestisce una richiesta per l'ottenimento di tutti gli Sprint di un prodotto
     * @param productId identificativo del prodotto di cui cercare gli Sprint
     * @return l'oggetto BacklogItemDto che rappresenta l'item inserito
     */
    @RequestMapping(path = "/{productId}", method = RequestMethod.GET)
    public ResponseEntity addBacklogItem(@PathVariable Long productId){
        try {
            List<SprintDTO> sprintDTOs = sprintCreateController.getAllByProduct(productId);
            return new ResponseEntityBuilder<>(sprintDTOs).setStatus(HttpStatus.OK).build();
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @JsonView(JsonViews.Basic.class)
    @RequestMapping(path = "close/{id}", method = RequestMethod.POST)
    public ResponseEntity closeSprint(@PathVariable Long id) {

        Sprint sprint;
        try {
            sprintCreateController.closeSprint(id);

        } catch (Exception e) {
            return CommonResponseEntity.NotFoundResponseEntity("ERRORE NELLA CHIUSURA\n" + e.getMessage());
        }
        return CommonResponseEntity.CreatedResponseEntity("CLOSED", "Sprint");
    }

    /**
     * Metodo che gestisce una richiesta per l'ottenimento di tutte le date all'iterno di uno sprint
     * @param sprintId identificativo dello sprint
     * @return la lista delle date
     */
    @RequestMapping(path = "/getDates/{sprintId}", method = RequestMethod.GET)
    public ResponseEntity getDates(@PathVariable Long sprintId){

        try {
            List<String> dates = sprintCreateController.getDates(sprintId);
            return new ResponseEntityBuilder<>(dates).setStatus(HttpStatus.OK).build();
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Metodo che gestisce una richiesta per l'attivazione di uno sprint
     * @param sprintId identificativo dello sprint
     */
    @RequestMapping(path = "/activate/{sprintId}", method = RequestMethod.PUT)
    public ResponseEntity activateSprint(@PathVariable Long sprintId){
        try {
            sprintCreateController.activateSprint(sprintId);
            return new ResponseEntityBuilder<>().setStatus(HttpStatus.OK).build();
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}