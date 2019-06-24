package com.isssr.ticketing_system.rest;

import com.isssr.ticketing_system.controller.ScrumCeremonyController;
import com.isssr.ticketing_system.dto.ScrumCeremonyDto;
import com.isssr.ticketing_system.exception.EntityNotFoundException;
import com.isssr.ticketing_system.exception.ScrumCeremonyNotSavedException;
import com.isssr.ticketing_system.response_entity.ResponseEntityBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("scrumCeremony")
@CrossOrigin("*")
public class ScrumCeremonyRest {

    @Autowired
    private ScrumCeremonyController scrumCeremonyController;

    /**
     * Metodo che gestisce una richiesta per creare una Scrum Ceremony
     * @param scrumCeremonyDto il dto della Scrum Ceremony da inserire
     * @return l'oggetto scrumCeremonyDto che rappresenta la scrum ceremony inserita
     */
    @RequestMapping(path = "/", method = RequestMethod.POST)
    public ResponseEntity addScrumProductWorkflow(@RequestBody ScrumCeremonyDto scrumCeremonyDto){
        try {
            ScrumCeremonyDto addedScrumCeremony =
                    scrumCeremonyController.addScrumCeremony(scrumCeremonyDto);
            return new ResponseEntityBuilder<>(addedScrumCeremony).setStatus(HttpStatus.CREATED).build();
        } catch (ScrumCeremonyNotSavedException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Metodo che gestisce una richiesta per ottenere le scrum ceremonies di uno sprint
     * @param sprintId identificativo dello sprint di cui bisogna cercare le scrum ceremonies
     * * @return la lista di scrum ceremonies trovate
     */
    @RequestMapping(path="/sprint/{sprintId}", method=RequestMethod.GET)
    public ResponseEntity getSprintScrumCeremonies(@PathVariable Long sprintId) {
        List<ScrumCeremonyDto> foundCeremonies = null;
        try {
            foundCeremonies = scrumCeremonyController.findScrumCeremoniesBySprint(sprintId);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntityBuilder<>(foundCeremonies).setStatus(HttpStatus.OK).build();
    }

}
