package com.isssr.ticketing_system.rest;

import com.fasterxml.jackson.annotation.JsonView;
import com.isssr.ticketing_system.controller.TargetController;
import com.isssr.ticketing_system.dto.ScrumAssignmentDto;
import com.isssr.ticketing_system.dto.TargetDto;
import com.isssr.ticketing_system.enumeration.*;
import com.isssr.ticketing_system.exception.NotFoundEntityException;
import com.isssr.ticketing_system.entity.Target;
import com.isssr.ticketing_system.response_entity.HashMapResponseEntityBuilder;
import com.isssr.ticketing_system.response_entity.JsonViews;
import com.isssr.ticketing_system.validator.ProductValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Questa classe gestisce le richieste HTTP che giungono sul path specificato ("targets")
 * attraverso i metodi definiti nella classe TargetController.
 */
@Validated
@RestController
@RequestMapping("targets")
@CrossOrigin("*")
public class TargetRest {

    private TargetController targetController;
    private ProductValidator productValidator;

    public TargetRest(TargetController targetController, ProductValidator productValidator) {
        this.targetController = targetController;
        this.productValidator = productValidator;
    }

    /**
     * Configura un validator per gli oggetti di tipo Team
     *
     * @param binder binder
     */
    @InitBinder
    public void setupBinder(WebDataBinder binder) {
        binder.addValidators(productValidator);
    }


    /**
     * Metodo usato per recuperare i metadati di interesse per un oggetto Target
     *
     * @return HashMap contenente i metadati richiesti
     */
    @JsonView(JsonViews.Basic.class)
    @RequestMapping(path = "metadata", method = RequestMethod.GET)
    public ResponseEntity getMetadata(@AuthenticationPrincipal Principal principal) {
        List<TargetType> targetTypes = Arrays.asList(TargetType.values());

        return new HashMapResponseEntityBuilder(HttpStatus.OK)
                .set("targetTypes", targetTypes)
                .build();
    }

    /**
     * Metodo usato per la gestione di una POST che arriva sull'url specificato. A fronte di
     * una richiesta di questo tipo il target viene inserito nel DB.
     *
     * @param target target che va aggiunto al DB.
     * @return target aggiunto al DB + esito della richiesta HTTP.
     * @see com.isssr.ticketing_system.controller.TargetController
     */
    @JsonView(JsonViews.DetailedTarget.class)
    @RequestMapping(path = "", method = RequestMethod.POST)
    public ResponseEntity<Target> insertTarget(@Valid @RequestBody Target target) {
        Target createdTarget = targetController.insertTarget(target);
        return new ResponseEntity<>(createdTarget, HttpStatus.CREATED);
    }

    /**
     * Metodo usato per la gestione di una GET che arriva sull'url specificato. A fronte di
     * una richiesta di questo tipo viene restuito il target con id specificato.
     *
     * @param id Id del target da cercare.
     * @return target cercato + esito della richiesta HTTP
     * @see com.isssr.ticketing_system.controller.TargetController
     */
    @JsonView(JsonViews.DetailedTarget.class)
    @RequestMapping(path = "{id}", method = RequestMethod.GET)
    public ResponseEntity getTargetById(@PathVariable Long id) {
        Target target;
        try {
            target = targetController.getTargetById(id);
        } catch (NotFoundEntityException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(target, HttpStatus.OK);
    }

    /**
     * Metodo usato per la gestione di una PUT che arriva sull'url specificato. A fronte di
     * una richiesta di questo tipo il target specificato presente nel DB viene aggiornato.
     *
     * @param id Id del target da aggiornare.
     * @param target target con le info aggiornate da mettere nel DB.
     * @return target eventualmente aggiornato + esito della richiesta HTTP.
     * @see com.isssr.ticketing_system.controller.TargetController
     */
    @JsonView(JsonViews.DetailedTarget.class)
    @RequestMapping(path = "{id}", method = RequestMethod.PUT)
    public ResponseEntity<Target> updateTargetById(@PathVariable Long id, @Valid @RequestBody Target target) {
        Target updatedTarget;
        try {
            updatedTarget = targetController.updateTargetById(id, target);
        } catch (NotFoundEntityException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(updatedTarget, HttpStatus.OK);
    }

    /**
     * Metodo usato per la gestione di una DELETE che arriva sull'url specificato. A fronte di
     * una richiesta di questo tipo il target specificato presente nel DB viene eliminato.
     *
     * @param id Id del target da eliminare.
     * @return target eventualmente eliminato + esito della richiesta HTTP.
     * @see com.isssr.ticketing_system.controller.TargetController
     */
    @JsonView(JsonViews.DetailedTarget.class)
    @RequestMapping(path = "{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Boolean> deleteTargetById(@PathVariable Long id) {
        boolean deleted;
        try {
            deleted = targetController.deleteTargetById(id);
        } catch (NotFoundEntityException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(deleted, HttpStatus.OK);
    }


    /**
     * Metodo usato per la gestione di una GET che arriva sull'url specificato. A fronte di
     * una richiesta di questo tipo vengono restuiti tutti i target presenti nel DB.
     *
     * @return target presenti nel DB + esito della richiesta HTTP
     * @see com.isssr.ticketing_system.controller.TargetController
     */
    @JsonView(JsonViews.DetailedTarget.class)
    @RequestMapping(path="", method = RequestMethod.GET)
    public ResponseEntity getAllTargets() {
        List<Target> targetList = targetController.getAllTargets();

        List<TargetDto> targetDtos = new ArrayList<>();

        for (Target item: targetList) {

            TargetDto targetDTO = new TargetDto();
            targetDTO.setId(item.getId());
            targetDTO.setDescription(item.getDescription());
            targetDTO.setName(item.getName());
            targetDTO.setStateMachineName(item.getStateMachineName());
            targetDTO.setVersion(item.getVersion());
            targetDTO.setTargetType(item.getTargetType().toString());
            targetDTO.setTargetState(item.getTargetState().toString());

            try {

                targetDTO.setScrumTeamId(item.getScrumTeam().getId());

            } catch (Exception e) {

                targetDTO.setScrumTeamId((long) -1);
            }

            targetDtos.add(targetDTO);
        }

        if(targetDtos !=null)
            return new ResponseEntity<>(targetDtos, HttpStatus.OK);
        else
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Metodo usato per la gestione di una GET che arriva sull'url specificato. A fronte di
     * una richiesta di questo tipo vengono restuiti tutti i target attivi presenti nel DB.
     *
     * @return target presenti nel DB + esito della richiesta HTTP
     * @see com.isssr.ticketing_system.controller.TargetController
     */
    @RequestMapping(path = "/active", method = RequestMethod.GET)
    public ResponseEntity<List<Target>> getActiveTargets() {
        List<Target> targetList = targetController.getActiveTargets();
        if(targetList !=null)
            return new ResponseEntity<>(targetList, HttpStatus.OK);
        else
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Metodo usato per la gestione di una DELETE che arriva sull'url specificato. A fronte di
     * una richiesta di questo tipo tutti i target presenti nel sistema vengono eliminati
     *
     * @return esito della richiesta HTTP.
     * @see com.isssr.ticketing_system.controller.TargetController
     */
    @JsonView(JsonViews.DetailedTarget.class)
    @RequestMapping(path = "", method = RequestMethod.DELETE)
    public ResponseEntity deleteAll() {
        targetController.deleteAll();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Metodo usato per la gestione di una PUT che arriva sull'url specificato. A fronte di
     * una richiesta di questo tipo lo stato del target specificato viene cambiato in
     * in RETIRED.
     *
     * @param id Id del target il cui stato va aggiornato.
     * @return esito della richiesta HTTP.
     * @see com.isssr.ticketing_system.controller.TargetController
     */
    @RequestMapping(path="/retire/{id}",method = RequestMethod.PUT)
    public ResponseEntity<Target> retireTarget(@PathVariable Long id) {
        Target updatedTarget;
        try {
            updatedTarget = targetController.changeStateTarget(id,TargetState.RETIRED);
        } catch (NotFoundEntityException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(updatedTarget, HttpStatus.OK);
    }

    /**
     * Metodo usato per la gestione di una PUT che arriva sull'url specificato. A fronte di
     * una richiesta di questo tipo lo stato del target specificato viene cambiato in
     * in ACTIVE.
     *
     * @param id Id del target il cui stato va aggiornato.
     * @return esito della richiesta HTTP.
     * @see com.isssr.ticketing_system.controller.TargetController
     */
    @RequestMapping(path="/rehab/{id}",method = RequestMethod.PUT)
    public ResponseEntity<Target> rehabTarget(@PathVariable Long id) {
        Target updatedTarget;
        try {
            updatedTarget = targetController.changeStateTarget(id, TargetState.ACTIVE);
        } catch (NotFoundEntityException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(updatedTarget, HttpStatus.OK);
    }

    /**
     * Dato un Target trova la sua SM e calcola quali sono gli stati gestiti da un determinato SystemRole
     *
     * @param targetID ID del target di cui occorre gestire la SM
     * @param role Ruolo del sistema di cui si desiderano gli stati Gestiti
     * @return La lista degli stati Gestiti da role per la SM del target con id targetID
     */
    @RequestMapping(path = "getActualStates/{targetID}/{systemRole}")
    public ResponseEntity<List<String>> getActualStates(@PathVariable("targetID") Long targetID, @PathVariable("systemRole")
            String role){
        List<String> states;
        try {
            states = targetController.getActualStates(targetID,role);
        } catch (NotFoundEntityException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if(states.size()!=0)
            return  new ResponseEntity<>(states,HttpStatus.OK);
        else return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Dato un Target trova la sua SM e calcola quali sono  gli stati successivi a partire da uno stato corrente currentTicketStatus
     *
     * @param targetID ID del target di cui gestire la SM
     * @param currentState Stato corrente di cui trovare i successivi
     * @return La lista degli stati successivi a currentTicketStatus per la SM del target con id targetID
     */
    @RequestMapping(path = "getNextStates/{targetID}/{currentState}")
    public ResponseEntity<ArrayList<ArrayList<String>>> getNextStates(@PathVariable("targetID") Long targetID, @PathVariable("currentState")
            String currentState){
        ArrayList<ArrayList<String>> states;
        try {
            states = targetController.getNextStates(targetID,currentState);
        } catch (NotFoundEntityException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if(states.size()!=0)
            return  new ResponseEntity<>(states,HttpStatus.OK);
        else return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Restituisce tutti gli assegnamenti dei prodotti Scrum agli scrum team
     * @return La lista degli assegnamenti
     */
    @RequestMapping(path = "scrumAssignments")
    public ResponseEntity<List<ScrumAssignmentDto>> getAllScrumProductAssignments() {
        List<ScrumAssignmentDto> assignments = targetController.getScrumAssignments();
        return new ResponseEntity<>(assignments, HttpStatus.OK);
    }

}
