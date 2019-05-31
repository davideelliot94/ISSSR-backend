package com.isssr.ticketing_system.rest;

import com.isssr.ticketing_system.controller.BacklogManagementController;
import com.isssr.ticketing_system.dto.BacklogItemDto;
import com.isssr.ticketing_system.exception.BacklogItemNotSavedException;
import com.isssr.ticketing_system.exception.TargetNotFoundException;
import com.isssr.ticketing_system.response_entity.ResponseEntityBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("backlog")
@CrossOrigin("*")
/* Questa classe esporta gli endpoint attraverso i quali è possibile interagire con il sistema di gestione del backlog*/
public class BacklogManagementRest {

    @Autowired
    private BacklogManagementController backlogManagementController;

    /**
     * Metodo che gestisce una richiesta per inserire un item all'interno di un product backlog
     * @param item il dto dell'item da inserire all'interno del product backlog
     * @param targetId identificativo del prodotto a cui è associato l'item
     * @return l'oggetto BacklogItemDto che rappresenta l'item inserito
     */
    @RequestMapping(path = "/target/{targetId}/item", method = RequestMethod.POST)
    public ResponseEntity addBacklogItem(@PathVariable Long targetId, @RequestBody BacklogItemDto item){
        try {
            BacklogItemDto addedItem = backlogManagementController.addBacklogItem(targetId, item);
            return new ResponseEntityBuilder<>(addedItem).setStatus(HttpStatus.CREATED).build();
        } catch (TargetNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (BacklogItemNotSavedException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
