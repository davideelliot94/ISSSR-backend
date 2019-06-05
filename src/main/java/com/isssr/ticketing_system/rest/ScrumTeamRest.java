package com.isssr.ticketing_system.rest;

import com.fasterxml.jackson.annotation.JsonView;
import com.isssr.ticketing_system.controller.ScrumTeamController;
import com.isssr.ticketing_system.controller.UserController;
import com.isssr.ticketing_system.entity.ScrumTeam;
import com.isssr.ticketing_system.dto.ScrumTeamDto;
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

import com.isssr.ticketing_system.response_entity.ResponseEntityBuilder;

//@Validated
@RestController
@RequestMapping(path = "scrumteams")
@CrossOrigin("*")
public class ScrumTeamRest {
    @Autowired
    private ScrumTeamController scrumTeamController;

    /**
     * Metodo usato per la gestione di una POST che arriva sull'url specificato. A fronte di
     * una richiesta di questo tipo lo scrum team viene inserito nel DB.
     *
     * @param scrumTeam  scrum team che va aggiunto al DB.
     * @return info del team aggiunto al DB + esito della richiesta HTTP.
     * @see com.isssr.ticketing_system.controller.ScrumTeamController
     */
    //@JsonView(JsonViews.DetailedScrumTeam.class)
    @RequestMapping(path = "/", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<ScrumTeamDto> insertScrumTeam(@RequestBody ScrumTeamDto scrumTeam) {
        try {
            System.out.println(("----   inserting in rest "));
            ScrumTeamDto createdScrumTeam = scrumTeamController.insertScrumTeam(scrumTeam);
            return new ResponseEntityBuilder<ScrumTeamDto>(createdScrumTeam).setStatus(HttpStatus.CREATED).build();
            //return new ResponseEntity<>(createdScrumTeam, HttpStatus.CREATED);
        } catch (InvalidScrumTeamException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

}
