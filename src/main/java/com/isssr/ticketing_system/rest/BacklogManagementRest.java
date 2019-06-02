package com.isssr.ticketing_system.rest;

import com.isssr.ticketing_system.controller.BacklogManagementController;
import com.isssr.ticketing_system.dto.BacklogItemDto;
import com.isssr.ticketing_system.dto.TargetDto;
import com.isssr.ticketing_system.exception.BacklogItemNotSavedException;
import com.isssr.ticketing_system.exception.EntityNotFoundException;
import com.isssr.ticketing_system.exception.SprintNotActiveException;
import com.isssr.ticketing_system.exception.TargetNotFoundException;
import com.isssr.ticketing_system.response_entity.ResponseEntityBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /**
     * Metodo che gestisce una richiesta per ottenere tutti i prodotti sul quale sta lavorando uno Scrum Team di cui
     * fa parte l'utente specificato.
     * @param username l'username dell'utente.
     * @return una lista di TargetDto corrispondenti ai prodotti sul quale sta lavorando uno Scrum Team di cui fa
     *         parte l'utente. Errore di richiesta invalida se l'utente passato come parametro non esiste.
     */
    @RequestMapping(path = "/product/user/{username}", method = RequestMethod.GET)
    public ResponseEntity addBacklogItem(@PathVariable String username){
        try {
            List<TargetDto> products = backlogManagementController.findProductByScrumUser(username);
            return new ResponseEntityBuilder<>(products).setStatus(HttpStatus.OK).build();
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Metodo che gestisce una richiesta per ottenere tutti gli Item all'interno del backlog di un dato prodotto.
     * @param productId l'identificativo del prodotto di cui ricercare gli item.
     * @return una lista di BacklogItemDto corrispondenti agli item presenti nel backlog del prodotto selezionato.
     *         Errore di richiesta invalida se il prodotto passato come parametro non esiste.
     * NB: Il metodo restituisce gli elementi nel Product Backlog del prodotto, non quelli dello Sprint Backlog
     */
    @RequestMapping(path = "/items/product/{productId}", method = RequestMethod.GET)
    public ResponseEntity getProductBacklogItem(@PathVariable Long productId){
        try {
            List<BacklogItemDto> items = backlogManagementController.findBacklogItemByProduct(productId);
            return new ResponseEntityBuilder<>(items).setStatus(HttpStatus.OK).build();
        } catch (TargetNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Metodo che gestisce una richiesta per inserire un item all'interno di uno Sprint Backlog. Se in fase di inserimento
     * nello Sprint Backlog l'item è stato aggiornato, le modifiche vengono memorizzate.
     * @param item il dto dell'item da inserire all'interno dello sprint backlog
     * @param targetId identificativo del prodotto a cui è associato l'item
     * @return l'oggetto BacklogItemDto che rappresenta l'item inserito nello Sprint Backlog
     */
    @RequestMapping(path = "/target/{targetId}/item/sprint", method = RequestMethod.PUT)
    public ResponseEntity addBacklogItemToSprintBacklog(@PathVariable Long targetId, @RequestBody BacklogItemDto item){
        try {
            BacklogItemDto addedItem = backlogManagementController.addBacklogItemToSprintBacklog(targetId, item);
            return new ResponseEntityBuilder<>(addedItem).setStatus(HttpStatus.OK).build();
        } catch (TargetNotFoundException | SprintNotActiveException e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Metodo che gestisce una richiesta per ottenere tutti gli Item all'interno dello sprint backlog attivo di un dato prodotto.
     * @param productId l'identificativo del prodotto di cui ricercare gli item.
     * @return una lista di BacklogItemDto corrispondenti agli item presenti nello sprint backlog attivo del prodotto
     *          selezionato. Errore di richiesta invalida se il prodotto passato come parametro non esiste.
     */
    @RequestMapping(path = "/items/product/{productId}/sprint", method = RequestMethod.GET)
    public ResponseEntity getSprintBacklogItem(@PathVariable Long productId){
        try {
            List<BacklogItemDto> items = backlogManagementController.findSprintBacklogItemByProduct(productId);
            return new ResponseEntityBuilder<>(items).setStatus(HttpStatus.OK).build();
        } catch (TargetNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (SprintNotActiveException e) {
            System.out.println("sto rispondendo picche");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
