package com.isssr.ticketing_system.rest;


import com.isssr.ticketing_system.controller.ScrumProductWorkflowController;
import com.isssr.ticketing_system.dto.ScrumProductWorkflowDto;
import com.isssr.ticketing_system.entity.ScrumProductWorkflow;
import com.isssr.ticketing_system.exception.NotFoundEntityException;
import com.isssr.ticketing_system.exception.ScrumProductWorkflowNotSavedException;
import com.isssr.ticketing_system.exception.UpdateException;
import com.isssr.ticketing_system.response_entity.ResponseEntityBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("scrumProductWorkflow")
@CrossOrigin("*")
/* Questa classe esporta gli endpoint attraverso i quali è possibile interagire con il sistema di gestione per il
* workflow dei prodotti Scrum*/

public class ScrumProductWorkflowRest {

    @Autowired
    private ScrumProductWorkflowController scrumProductWorkflowController;

    /**
     * Metodo che gestisce una richiesta per creare un workflow per prodotti Scrum
     * @param scrumProductWorkflowDto il dto del workflow da inserire
     * @return l'oggetto scrumProductWorkflowDto che rappresenta l'item inserito
     */
    @RequestMapping(path = "/", method = RequestMethod.POST)
    public ResponseEntity addScrumProductWorkflow(@RequestBody ScrumProductWorkflowDto scrumProductWorkflowDto){
        try {
            ScrumProductWorkflowDto addedWorkflow =
                    scrumProductWorkflowController.addScrumProductWorkflow(scrumProductWorkflowDto);
            return new ResponseEntityBuilder<>(addedWorkflow).setStatus(HttpStatus.CREATED).build();
        } catch (ScrumProductWorkflowNotSavedException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Metodo che gestisce una richiesta ottenere l'elenco di tutti i workflow per prodotti Scrum
     * @return lista di scrumProductWorkflowDto presenti nel sistema
     */
    @RequestMapping(path = "/", method = RequestMethod.GET)
    public ResponseEntity listScrumProductWorkflows(){
        try {
            List<ScrumProductWorkflowDto> scrumProductWorkflowDtos =
                    scrumProductWorkflowController.getAllScrumProductWorkflow();
            return new ResponseEntityBuilder<>(scrumProductWorkflowDtos).setStatus(HttpStatus.OK).build();
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Metodo che gestisce una richiesta per eliminare un workflow per prodotti Scrum
     * @param scrumProductWorkflowId l'identificativo del workflow da eliminare
     */
    @RequestMapping(path = "/{scrumProductWorkflowId}", method = RequestMethod.DELETE)
    public ResponseEntity removeScrumProductWorkflow(@PathVariable Long scrumProductWorkflowId){
        try {
            scrumProductWorkflowController.removeScrumProductWorkflow(scrumProductWorkflowId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NotFoundEntityException e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (UpdateException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    /**
     * Metodo che gestisce una richiesta per aggiornare un workflow per prodotti Scrum
     * @param scrumProductWorkflowDto il dto del workflow da modificare
     */
    @RequestMapping(path = "/", method = RequestMethod.PUT)
    public ResponseEntity updateScrumProductWorkflow(@RequestBody ScrumProductWorkflowDto scrumProductWorkflowDto){
        try {
            ScrumProductWorkflowDto updatedWorkflow =
                scrumProductWorkflowController.updateScrumProductWorkflow(scrumProductWorkflowDto);
            return new ResponseEntityBuilder<>(updatedWorkflow).setStatus(HttpStatus.OK).build();
        } catch (NotFoundEntityException e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (UpdateException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }
}
