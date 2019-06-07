package com.isssr.ticketing_system.rest;

import com.isssr.ticketing_system.controller.ScrumTeamController;
import com.isssr.ticketing_system.dto.ScrumTeamDto;
import com.isssr.ticketing_system.exception.InvalidScrumTeamException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.isssr.ticketing_system.response_entity.ResponseEntityBuilder;

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
    @RequestMapping(path = "/", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<ScrumTeamDto> insertScrumTeam(@RequestBody ScrumTeamDto scrumTeam) {
        try {
            ScrumTeamDto createdScrumTeam = scrumTeamController.insertScrumTeam(scrumTeam);
            return new ResponseEntityBuilder<ScrumTeamDto>(createdScrumTeam).setStatus(HttpStatus.CREATED).build();
        } catch (InvalidScrumTeamException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

}
