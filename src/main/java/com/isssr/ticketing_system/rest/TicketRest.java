package com.isssr.ticketing_system.rest;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.collect.Lists;
import com.isssr.ticketing_system.configuration.ConfigProperties;
import com.isssr.ticketing_system.enumeration.*;
import com.isssr.ticketing_system.exception.*;
import com.isssr.ticketing_system.mail.mailHandler.MailSenderHandler;
import com.isssr.ticketing_system.entity.*;
import com.isssr.ticketing_system.response_entity.*;
import com.isssr.ticketing_system.controller.*;
import com.isssr.ticketing_system.validator.TicketValidator;
import org.apache.commons.io.IOUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.*;
import java.security.Principal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.springframework.http.HttpStatus.OK;


/**
 * Questa classe gestisce le richieste HTTP che giungono sul path specificato ("ticket")
 * attraverso i metodi definiti nella classe TicketController.
 */
@Validated
@RestController
@RequestMapping("tickets")
@CrossOrigin("*")
public class TicketRest {

    private TicketController ticketController;
    private UserController userController;
    private TargetController targetController;
    private MailSenderHandler mailSenderController;
    private TicketValidator ticketValidator;


    // Directory usata per memorizzare gli allegati di tutti gli utenti
    private final String ATTACHED_FILES_SYSTEM_PATH;
    // Directory usata per memorizzare gli allegati di un utente
    private final String ATTACHED_FILES_USER_PATH;
    // Filename di un allegato di un utente
    private final String ATTACHED_FILE_PATH;


    @Autowired
    public TicketRest(
            TicketController ticketController,
            UserController userController,
            TargetController targetController,
            MailSenderHandler mailSenderController,
            TicketValidator ticketValidator,
            ConfigProperties configProperties
    ) {
        this.ticketController = ticketController;
        this.userController = userController;
        this.targetController = targetController;
        this.mailSenderController = mailSenderController;
        this.ticketValidator = ticketValidator;

        // BASE_SYS_PATH
        ATTACHED_FILES_SYSTEM_PATH = configProperties.getAttachementsStorage();
        // ATTACHED_FILES_SYSTEM_PATH + /username
        ATTACHED_FILES_USER_PATH = ATTACHED_FILES_SYSTEM_PATH + "/%s";
        // ATTACHED_FILES_USER_PATH + /attached_id_subId
        ATTACHED_FILE_PATH = ATTACHED_FILES_USER_PATH + "/attached_%d_%d";
    }

    /**
     * Configura un validator per gli oggetti di tipo Ticket
     *
     * @param binder binder
     */
    @InitBinder("ticket")
    public void setupBinder(WebDataBinder binder) {
        binder.addValidators(ticketValidator);
    }


    /**
     * Metodo usato per recuperare i metadati di interesse per un oggetto Ticket
     *
     * @return HashMap contenente i metadati richiesti
     */
    @JsonView(JsonViews.Basic.class)
    @RequestMapping(path = "metadata", method = RequestMethod.GET)
    public ResponseEntity getMetadata() {
        //Optional<User> user = userController.findByEmail(principal.getName());

        List<Visibility> visibilities = Arrays.asList(Visibility.values());
        //Collection<User> assignees = user.get().getTeam().getTeamMembers();
        List<TicketCategory> categories = Arrays.asList(TicketCategory.values());
        Iterable<Target> targets = targetController.getAllTargets();
        List<TicketPriority> priorities = Arrays.asList(TicketPriority.values());
        List<TicketStatus> statuses = Arrays.asList(TicketStatus.values());
        List<Target> activeTargets = targetController.getActiveTargets();

        return new HashMapResponseEntityBuilder(HttpStatus.OK)
                .set("visibilities", visibilities)
                //.set("assignees", assignees)
                .set("categories", categories)
                .set("targets", StreamSupport.stream(targets.spliterator(), false).collect(Collectors.toList()))
                .set("priorities", priorities)
                .set("statuses", statuses)
                .set("activeTargets", activeTargets)
                .build();
    }

    /**
     * Metodo usato per la gestione di una POST che arriva sull'url specificato. A fronte di
     * una richiesta di questo tipo il ticket viene inserito nel DB.
     * @param ticket che va aggiunto al DB.
     * @return info del ticket aggiunto al DB + esito della richiesta HTTP.
     * @see com.isssr.ticketing_system.controller.TicketController
     */
    @JsonView(JsonViews.DetailedTicket.class)
    @RequestMapping(path = "", method = RequestMethod.POST)
    public ResponseEntity insertTicket(@Valid @RequestBody Ticket ticket, @AuthenticationPrincipal Principal principal) throws EntityNotFoundException {
        ticket.setCreationTimestamp(Instant.now());
        ticket.setSource(TicketSource.CLIENT);
        ticket.setCurrentTicketStatus(TicketStatus.VALIDATION);
        User customer = userController.findUserByUsername(principal.getName());
        ticket.setCustomer(customer);
        try {
            ticket.setAttachments(savedFiles(
                    ticket.getAttachments(),
                    ticket.getCustomer().getUsername()
            ));
        } catch (FileUploadException e) {
            return CommonResponseEntity.NotFoundResponseEntity("ENTITY_NOT_FOUND");
        }
        ticketController.insertTicket(ticket);

        mailSenderController.sendMail(customer.getEmail(), "TICKET_OPENED");

        return CommonResponseEntity.CreatedResponseEntity("CREATED");
    }



    /**
     * Metodo usato per la gestione di una GET che arriva sull'url specificato. A fronte di
     * una richiesta di questo tipo viene restituito il ticket che ha l'id specificato.
     *
     * @param id ID del ticket da restituire.
     * @return Ticket presente nel DB che risponde ai criteri sopraspecificati + esito della richiesta HTTP.
     * @see com.isssr.ticketing_system.controller.TicketController
     */
    @JsonView(JsonViews.Basic.class)
    @RequestMapping(path = "{id}", method = RequestMethod.GET)
    public ResponseEntity getTicketById(@PathVariable Long id) {
        Ticket ticket = null;
        try {
            ticket = ticketController.getTicketById(id);
        } catch (EntityNotFoundException e) {
            return CommonResponseEntity.NotFoundResponseEntity("TICKET_NOT_FOUND");
        }
        
        return new ResponseEntityBuilder<>(ticket).setStatus(HttpStatus.OK).build();
    }


    /**
     * Metodo usato per la gestione di una PUT che arriva sull'url specificato. A fronte di
     * una richiesta di questo tipo le info relative al ticket specificato
     * vengono aggiornate nel DB.
     *
     * @param id Id del ticket da aggiornare.
     * @param ticket aggiornato le cui info aggiornate vanno inserite nel DB.
     * @return ticket con le info aggiornate + esito della richiesta HTTP.
     * @see com.isssr.ticketing_system.controller.TicketController
     */
    @JsonView(JsonViews.DetailedTicket.class)
    @RequestMapping(path = "{id}", method = RequestMethod.PUT)
    public ResponseEntity updateTicketById(@PathVariable Long id, @Valid @RequestBody Ticket ticket) {
        Ticket updatedTicket;

        try {
            updatedTicket = ticketController.updateById(id, ticket);
        } catch (EntityNotFoundException e) {
            return CommonResponseEntity.NotFoundResponseEntity("TICKET_NOT_FOUND");
        }

        return new ResponseEntity<>(updatedTicket, HttpStatus.OK);
    }


    /**
     * Metodo usato per la gestione di una DELETE che arriva sull'url specificato. A fronte di
     * una richiesta di questo tipo il ticket specificato viene eliminato dal DB.
     *
     * @param id id del ticket che deve essere eliminato.
     * @return esito della richiesta HTTP.
     * @see com.isssr.ticketing_system.controller.TicketController
     */
    @JsonView(JsonViews.DetailedTicket.class)
    @RequestMapping(path = "{id}", method = RequestMethod.DELETE)
    //@PreAuthorize("hasAuthority('WRITE_PRIVILEGE')")
    public ResponseEntity delete(@PathVariable Long id) {
        boolean deleted;

        try {
            deleted = ticketController.deleteTicketById(id);
        } catch (EntityNotFoundException e) {
            return CommonResponseEntity.NotFoundResponseEntity("TICKET_NOT_FOUND");
        }

        return CommonResponseEntity.OkResponseEntity("DELETED");
    }

    /**
     * Metodo usato per la gestione di una GET che arriva sull'url specificato. A fronte di
     * una richiesta di questo tipo vengono restituiti tutti i ticket presenti nel DB.
     *
     * @return lista dei ticket presenti nel DB + esito della richiesta HTTP.
     * @see com.isssr.ticketing_system.controller.TicketController
     */
    @JsonView(JsonViews.Basic.class)
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity getAllTickets() {
        List<Ticket> tickets = ticketController.getAllTickets();
        return new ResponseEntityBuilder<>(tickets).setStatus(HttpStatus.OK).build();
    }

    /**
     * Metodo usato per la gestione di una DELETE che arriva sull'url specificato. A fronte di
     * una richiesta di questo tipo tutti i ticket presenti nel DB vengono eliminati.
     *
     * @return esito della richiesta HTTP.
     * @see com.isssr.ticketing_system.controller.TicketController
     */
    @JsonView(JsonViews.DetailedTicket.class)
    @RequestMapping(method = RequestMethod.DELETE)
    public ResponseEntity deleteAll() {
        Long count = ticketController.count();

        if (count == 0)
            return CommonResponseEntity.NotFoundResponseEntity("TICKET_NOT_FOUND");

        ticketController.deleteAll();

        return CommonResponseEntity.OkResponseEntity("DELETED");
    }

    /**
     * Metodo usato per la gestione di una PUT che arriva sull'url specificato. A fronte di
     * una richiesta di questo tipo viene aggiornato il l'utente che ha in carico il ticket specificato.
     *
     * @param ticketId Id del ticket di cui bisogna modificare l'utente incaricato della risoluzione del ticket.
     * @param userId Id dell'utente da assegnare al ticket specificato.
     * @return Ticket presenti nel DB che rispondono ai criteri sopraspecificati + esito della richiesta HTTP.
     * @see com.isssr.ticketing_system.controller.TicketController
     */
    @RequestMapping(path = "{ticketId}/assignTo/{userId}", method = RequestMethod.PUT)
    @ResponseStatus(OK)
    public ResponseEntity assignTicket(@PathVariable Long ticketId, @PathVariable Long userId) throws EntityNotFoundException {
        Ticket ticket = ticketController.assignTicket(ticketId,userId);
        if(ticket != null)
            return new ResponseEntityBuilder<>(ticket).setStatus(HttpStatus.OK).build();
        else
            return CommonResponseEntity.NotFoundResponseEntity("TICKET_NOT_FOUND");


    }

    /**
     * Metodo usato per la gestione di una GET che arriva sull'url specificato. A fronte di
     * una richiesta di questo tipo vengono restiutiti tutti i ticket presenti nel DB aperti dall'utente che ha fatto la richiesta.
     *
     * @return Ticket presenti nel DB che rispondono ai criteri sopraspecificati + esito della richiesta HTTP.
     * @see com.isssr.ticketing_system.controller.TicketController
     */
    @GetMapping("customer")
    @ResponseStatus(OK)
    List<Ticket> getAllCustomerTicket() throws EntityNotFoundException {
        User customer = getCurrentUserFromSecurityContext();
        return ticketController.getTicketsByCustomer(customer);
    }

    /**
     * Metodo utilizzato per evitare che un team member possa visualizzare
     * un ticket privato acquisito da un altro assistant
     *
     * @param ticket ticket su cui effettuare il controllo
     * @param user utente sui cui effettuare il controllo
     * @param <S>
     * @return true se l'utente può accedere al ticket, false altrimenti
     * @throws ClassCastException
     */
    private <S extends User> boolean canAccessTo(Ticket ticket, User user) throws ClassCastException {
        S currentAssistant = (S) user;

        return !ticket.getVisibility().equals("PRIVATE") ||
                currentAssistant.getId().equals(ticket.getAssignee().getId());
    }

    /**
     * Ottiene il principal dell'utente che ha fatto la richiesta
     *
     * @return oggetto user details relativo all'utente che ha inviato la richiesta
     * @throws EntityNotFoundException l'utente non è presente nel DB.
     */
    private User getCurrentUserFromSecurityContext() throws EntityNotFoundException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        return userController.findUserByUsername(userDetails.getUsername());
    }



    /* Progetto Gestione pianificazione e relazioni */


    /**
     * Metodo usato per la gestione di una GET che arriva sull'url specificato. A fronte di
     * una richiesta di questo tipo vengono restiutiti tutti i ticket presenti nel DB aperti dall'utente indicato.
     * Restituisce i ticket di un utente
     *
     * @param user utente di cui si vogliono visualizzare i ticket aperti.
     * @return Ticket presenti nel DB che rispondono ai criteri sopraspecificati + esito della richiesta HTTP.
     * @see com.isssr.ticketing_system.controller.TicketController
     */
    @RequestMapping(path = "getTicketsByUser", method = RequestMethod.POST)
    public ResponseEntity getTicketsByUserId(@RequestBody User user) {
        List<Ticket> tickets = ticketController.getTicketsByCustomer(user);
        if(tickets==null || tickets.size()==0)
            return CommonResponseEntity.NotFoundResponseEntity("TICKET_NOT_FOUND");
        return new ResponseEntityBuilder<>(tickets).setStatus(HttpStatus.OK).build();
    }

    /**
     * Restituisce i ticket in relazione di uguaglianza con "ticket"
     *
     * @param ticket ticket di cui si vogliono i ticket in relazione di uguaglianza
     * @return lista di ticket in relazione di uguaglianza con il ticket specificato
     */
    @RequestMapping(path = "getTicketsBySameTicket", method = RequestMethod.POST)
    public ResponseEntity getTicketsBySameTicket(@RequestBody Ticket ticket) {
        List<Ticket> tickets = ticketController.findTicketBySameTicket(ticket);
        if(tickets==null || tickets.size()==0)
            return CommonResponseEntity.NotFoundResponseEntity("TICKET_NOT_FOUND");
        return new ResponseEntityBuilder<>(tickets).setStatus(HttpStatus.OK).build();
    }


    /**
     * Crea una relazione di equivalentza tra i ticket aventi Id "id" e quello presente nel campo sameTicket del
     * ticket "sameTicket" passato nel body della richiesta.
     * @param id identificativo del ticketA
     * @param sameTicketId non usato (correggere query dal FE)
     * @param sameTicket ticket contenente ticketB nel campo sameTicket
     * @return ticketA aggiornato
     */
    @RequestMapping(path = "addEqualityTicket/{id}/{sameTicketId}", method = RequestMethod.PUT)
    public ResponseEntity addEqualityTicket(@PathVariable Long id,@PathVariable Long sameTicketId, @RequestBody Ticket sameTicket) {
        Ticket updatedTicket;

        //TODO eliminare le seguenti due righe facendo in modo che il FE passi gli id come parametri nell url
        Long idTicketA = id;
        Long idTicketB = sameTicket.getSameTicket().getId();

        if (idTicketA.equals(idTicketB)){
            return CommonResponseEntity.FailedDependencyResponseEntity("BAD_REQUEST");
        }
        try {
            updatedTicket = ticketController.createEquivalentRelation(idTicketA, idTicketB);
            return new ResponseEntityBuilder<>(updatedTicket).setStatus(HttpStatus.OK).build();
        } catch (EquivalenceCycleException e) {
            // questo è il risultato quando si prova a creare un'equivalenza che avrebbe come effetto collaterale
            // la creazione di un ciclo di dipendenze.
            return CommonResponseEntity.FailedDependencyResponseEntity("FAILED_DEPENDENCY");
        } catch (NotFoundEntityException e) {
            return CommonResponseEntity.NotFoundResponseEntity("TICKET_NOT_FOUND");
        } catch (EquivalenceBlockingDependencyException e) {
            // questo è il valore restituito quando si prova a creare un'equivalenza fra un ticket che dipende
            // da un ticket non chiuso e un ticket che è già avanzato nel workflow
            return CommonResponseEntity.FailedDependencyResponseEntity("BLOCKING_DEPENDENCY");
        }
    }


    /**
     * Crea una relazione di dipendenza tra i ticket con id "id" (ticket padre) e "dependentTicketId (ticket figlio)
     *
     * @param id ide del ticket padre
     * @param dependentTicketID id del ticket figlio
     * @return lista di ticket + esito della richiesta HTTP
     */
    @RequestMapping(path = "addDependentTicket/{id}/{dependentTicketID}", method = RequestMethod.POST)
    public ResponseEntity addDependentTicket(@PathVariable Long id,@PathVariable Long dependentTicketID) {
        List<Ticket> cycle = new ArrayList<>();
        if (id.equals(dependentTicketID))
            return CommonResponseEntity.FailedDependencyResponseEntity("FAILED_DEPENDENCY");
        try {
            cycle = ticketController.addDependentTicket(id, dependentTicketID);
        }catch (NotFoundEntityException e){
            return CommonResponseEntity.NotFoundResponseEntity("TICKET_NOT_FOUND");
        }

        if(cycle.isEmpty())
            return new ResponseEntityBuilder<>(cycle).setStatus(HttpStatus.OK).build();
        else return CommonResponseEntity.FailedDependencyResponseEntity("FAILED_DEPENDENCY");

    }

    /**
     * Metodo usato per rilasciare un ticket e decrementare
     * le dipendenze di un ticket rilasciato
     * Nella richiesta inserire json con almeno id e stato(released)
     *
     * @param id del ticket rilasciato
     * @param ticket ticket rilasciato
     * @return ticket aggiornato + esito della richiesta HTTP
     */
    @RequestMapping(path = "release/{id}", method = RequestMethod.PUT)
    public ResponseEntity releaseTicket(@PathVariable Long id, @RequestBody Ticket ticket) {
        Ticket ticketRelease = null;
        try {
            ticketRelease = ticketController.releaseTicket(id,ticket);
        }catch (NotFoundEntityException e){
            return CommonResponseEntity.NotFoundResponseEntity("TICKET_NOT_FOUND");
        }

        return new ResponseEntityBuilder<>(ticketRelease).setStatus(HttpStatus.OK).build();

    }

    /**
     * Aggiunge una relazione di regressione al ticket con id specificato
     *
     * @param id id del ticket a cui aggiungere la relazione di regressione
     * @param idGenerator
     * @return
     */
    @RequestMapping(path = "addRegression/{id}/{idGenerator}", method = RequestMethod.POST)
    public ResponseEntity addRegression(@PathVariable Long id,@PathVariable Long idGenerator) {
        Ticket ticketRegression = null;
        if (id.equals(idGenerator))
            return CommonResponseEntity.FailedDependencyResponseEntity("FAILED_DEPENDENCY");
        try {
            ticketRegression = ticketController.addRegression(id, idGenerator);
        } catch (NotFoundEntityException e){
            return CommonResponseEntity.NotFoundResponseEntity("TICKET_NOT_FOUND");
        }

        return new ResponseEntityBuilder<>(ticketRegression).setStatus(HttpStatus.OK).build();
    }

//----------------- Relation -----------------------------

    /**
     * Ritorna i ticket senza una relazione
     *
     * @return lista di ticket senza una relazione + esito della richiesta HTTP
     */
    @RequestMapping(path = "findTicketNoRelation", method = RequestMethod.GET)
    public ResponseEntity findTicketNoRelation() {
        List<Ticket> tickets = ticketController.findTicketNoRelation();
        return new ResponseEntityBuilder<>(tickets).setStatus(HttpStatus.OK).build();
    }


    /**
     * Ritorna i ticket che possiedono una relazione di dipendenza
     *
     * @return lista di ticket + esito della richiesta HTTP
     */
    @RequestMapping(path = "findTicketDependency", method = RequestMethod.GET)
    public ResponseEntity findTicketDependency() {
        List<Ticket> tickets = ticketController.findTicketDependency();
        return new ResponseEntityBuilder<>(tickets).setStatus(HttpStatus.OK).build();
    }


    /**
     * Ritorna i ticket con i quali è possibile creare una relazione di uguaglianza
     *
     * @return lista di ticket + esito della richiesta HTTP
     */
    @RequestMapping(path = "findTicketForCreateEquality", method = RequestMethod.GET)
    public ResponseEntity findTicketForCreateEquality() {
        List<Ticket> tickets = ticketController.findTicketForCreateEquality();
        return new ResponseEntityBuilder<>(tickets).setStatus(HttpStatus.OK).build();
    }


    /**
     * Ritorna i ticket con i quali è possibile creare una relazione di dipendenza
     *
     * @return lista di ticket + esito della richiesta HTTP
     */
    @RequestMapping(path = "findTicketForCreateDependency", method = RequestMethod.GET)
    public ResponseEntity findTicketForCreateDependency() {
        List<Ticket> tickets = ticketController.findTicketForCreateDependency();
        return new ResponseEntityBuilder<>(tickets).setStatus(HttpStatus.OK).build();
    }


    /**
     * Ritorna i ticket con i quali è possibile creare una relazione di regressione
     *
     * @return lista di ticket + esito della richiesta HTTP
     */
    @RequestMapping(path = "findTicketForCreateRegression", method = RequestMethod.GET)
    public ResponseEntity findTicketForCreateRegression() {
        List<Ticket> tickets = ticketController.findTicketForCreateRegression();
        return new ResponseEntityBuilder<>(tickets).setStatus(HttpStatus.OK).build();
    }

//------------------ Escalation ----------------------------------


    /**
     * Rest per ottenere i ticket in coda (pending)
     *
     * @return lista di ticket + esito della richiesta HTTP
     */
    @RequestMapping(path = "findTicketInQueue", method = RequestMethod.GET)
    public ResponseEntity findTicketInQueue() {
        List<Ticket> tickets = ticketController.findTicketInQueue();
        return new ResponseEntityBuilder<>(tickets).setStatus(HttpStatus.OK).build();
    }




//-------------- GANTT ---------------------------------


    /**
     * Metodo usato per ottenere tutti i ticket associati ad un team
     *
     * @param teamName
     * @return lista di ticket + esito della richiesta HTTP
     */
    @RequestMapping(path = "findTicketByTeam/{teamName}", method = RequestMethod.GET)
    public ResponseEntity findTicketByTeam(@PathVariable String teamName) throws EntityNotFoundException {
        List<Ticket> tickets = ticketController.findTicketByTeam(teamName);
        return new ResponseEntityBuilder<>(tickets).setStatus(HttpStatus.OK).build();
    }


    /**
     * Ritorna la lista dei ticket associata ad un team
     *
     * @return lista di ticket + esito della richiesta HTTP
     */
    @RequestMapping(path = "findTicketForGantt/{teamName}", method = RequestMethod.GET)
    public ResponseEntity findTicketForGantt(@PathVariable String teamName) throws EntityNotFoundException {
        List<Ticket> tickets = ticketController.findTicketForGanttByTeam(teamName);
        return new ResponseEntityBuilder<>(tickets).setStatus(HttpStatus.OK).build();
    }


    /**
     * Ritorna la lista di ticket da cui un ticket dipende
     *
     * @return lista di ticket + esito della richiesta HTTP
     */
    @RequestMapping(path = "findFatherTicket/{ticketId}", method = RequestMethod.GET)
    public ResponseEntity findFatherTicket(@PathVariable Long ticketId) {
        List<Ticket> tickets = ticketController.findFatherTicket(ticketId);
        return new ResponseEntityBuilder<>(tickets).setStatus(HttpStatus.OK).build();
    }

    /**
     * Metodo usato per cambiare il campo "difficulty" del Ticket
     *
     * @param id Id del ticket da aggiornare
     * @param difficulty Nuova difficoltà del Ticket
     * @return lista dei TeamMember senza Team
     */
    @RequestMapping(path = "changeDifficulty/{difficulty}/{id}", method = RequestMethod.PUT)
    public ResponseEntity updateTicketDifficulty(@PathVariable("id") Long id, @PathVariable("difficulty") TicketDifficulty difficulty) {
        Ticket updatedTicket;
        try {
            updatedTicket = ticketController.updateTicketDifficulty(id, difficulty);
        } catch (EntityNotFoundException e) {
            return CommonResponseEntity.NotFoundResponseEntity("TICKET_NOT_FOUND");
        }

        return new ResponseEntityBuilder<>(updatedTicket).setStatus(HttpStatus.OK).build();
    }


    /**
     * Metodo usato per cambiare il campo "priority" del Ticket
     *
     * @param id Id del ticket da aggiornare
     * @param ticketPriority Nuova difficoltà del Ticket
     * @return lista dei TeamMember senza Team
     */
    @RequestMapping(path = "changePriority/{priority}/{id}", method = RequestMethod.PUT)
    public ResponseEntity updateTicketPriority(@PathVariable("id") Long id, @PathVariable("priority") TicketPriority ticketPriority) {
        Ticket updatedTicket;
        try {
            updatedTicket = ticketController.updateTicketPriority(id, ticketPriority);
        } catch (EntityNotFoundException e) {
            return CommonResponseEntity.NotFoundResponseEntity("TICKET_NOT_FOUND");
        }

        return new ResponseEntityBuilder<>(updatedTicket).setStatus(HttpStatus.OK).build();
    }


    /**
     * Metodo usato per cambiare ticketPriority e tipo di un ticket
     *
     * @param id Id del ticket da aggiornare
     * @param ticketPriority Nuova difficoltà del Ticket
     * @param actualType tipo "actual" del ticket
     * @return lista dei TeamMember senza Team
     */
    @RequestMapping(path = "changePriorityAndType/{priority}/{actualType}/{id}", method = RequestMethod.PUT)
    public ResponseEntity updateTicketPriorityAndActualType(@PathVariable("id") Long id, @PathVariable("priority") TicketPriority ticketPriority, @PathVariable("actualType") String actualType) {
        Ticket updatedTicket;
        try {
            updatedTicket = ticketController.updateTicketPriorityAndActualType(id, ticketPriority,actualType);
        } catch (EntityNotFoundException e) {
            return CommonResponseEntity.NotFoundResponseEntity("TICKET_NOT_FOUND");
        }

        return new ResponseEntityBuilder<>(updatedTicket).setStatus(HttpStatus.OK).build();
    }

    /**
     * Metodo usato per la gestione di una GET che arriva sull'url specificato. A fronte di
     * una richiesta di questo tipo vengono restiutiti tutti i ticket presenti nel DB risolti dall'utente indicato.
     *
     * @param assigneeID teamLeader dell'utente di cui si vogliono visualizzare i ticket risolti.
     * @return Ticket presenti nel DB che rispondono ai criteri sopraspecificati + esito della richiesta HTTP.
     * @see com.isssr.ticketing_system.controller.TicketController
     */
    @RequestMapping(path="/ticketByAssignee/{teamLeaderID}",method = RequestMethod.GET)
    public ResponseEntity getTicketsByAssignee(@PathVariable("teamLeaderID") Long assigneeID){
        List<Ticket> tickets = null;
        try {
            tickets = ticketController.getTicketsByAssignee(assigneeID);
        } catch (EntityNotFoundException e) {
            return CommonResponseEntity.NotFoundResponseEntity("TICKET_NOT_FOUND");
        }

        return new ResponseEntityBuilder<>(tickets).setStatus(HttpStatus.OK).build();
    }

    /**
     * Metodo usato per la gestione di una GET che arriva sull'url specificato. A fronte di
     * una richiesta di questo tipo vengono restiutiti tutti i ticket presenti nel DB corrispondenti
     * allo stato indicato.
     *
     * @param ticketStatus Tipo di stato per cui si vogliono visualizzare i ticket.
     * @return Ticket presenti nel DB che rispondono ai criteri sopraspecificati + esito della richiesta HTTP.
     * @see com.isssr.ticketing_system.controller.TicketController
     */
    @RequestMapping(path = "/findTicketByStatus/{status}",method = RequestMethod.GET)
    public ResponseEntity getTicketByStatus(@PathVariable("status")TicketStatus ticketStatus){
        List<Ticket> tickets = ticketController.getTicketsByStatus(ticketStatus);
        return new ResponseEntityBuilder<>(tickets).setStatus(HttpStatus.OK).build();
        /*if(tickets != null)
            return new ResponseEntityBuilder<>(tickets).setStatus(HttpStatus.OK).build();
        else
            return CommonResponseEntity.NotFoundResponseEntity("TICKET_NOT_FOUND");
            */
    }



    /**
     * Servizio REST per l'inserimento di un commento in ticket.
     *
     * @param ticketID ID del ticket da commentare
     * @param ticketComment commento da allegare al ticket.
     * @return il ticket con allegato il commento.
     */
    @RequestMapping(path= "/insertComment/{ticketID}/{userID}",method = RequestMethod.POST)
    public ResponseEntity insertComment(@PathVariable("ticketID") Long ticketID, @PathVariable("userID") Long userID, @RequestBody TicketComment ticketComment){
        Ticket commentedTicket;
        try {
            commentedTicket = ticketController.insertComment(ticketID, userID, ticketComment);
        } catch(EntityNotFoundException e) {
            return CommonResponseEntity.NotFoundResponseEntity("TICKET_NOT_FOUND");
        }

        return new ResponseEntityBuilder<>(commentedTicket).setStatus(HttpStatus.OK).build();
    }


    /**
     * Metodo che salva i file specificati su disco
     *
     * @param username username dell'utente corrente
     * @param filesToSave lista di file da salvare
     * @return lista vuota
     * @throws IOException
     * @throws SecurityException
     */
    private List<String> saveAttachedFiles(String username, String[] filesToSave)
            throws IOException, SecurityException {
        if (filesToSave == null) {
            return null;
        }

        Long randomId = ThreadLocalRandom.current().nextLong();
        List<String> savedFiles = Lists.newArrayList();
        for (String str : filesToSave) {
            String userDirectoryString = String.format(
                    ATTACHED_FILES_USER_PATH,
                    username
            );

            File userDirectory = new File(userDirectoryString);
            if (!userDirectory.exists()) userDirectory.mkdirs();

            // TODO sostituisci i con lista filenames
            int i = 0;
            String fileToWriteString = String.format(
                    ATTACHED_FILE_PATH,
                    username,
                    randomId,
                    i
            );
            ++i;
            File fileToWrite = new File(fileToWriteString);

            if (!fileToWrite.exists() && !fileToWrite.createNewFile()) {
                throw new IOException();
            }

            OutputStream stream = new FileOutputStream(fileToWriteString);
            byte[] data = Base64.decodeBase64(str);
            stream.write(data);
            savedFiles.add(fileToWrite.getAbsolutePath());
        }

        return savedFiles;
    }


    /**
     *
     * @param str
     * @return
     * @throws BadFormatException
     */
    private String[] getDataFromString(String str) throws BadFormatException {
        if (str == null) {
            return null;
        }

        String[] tmp = str.split(",");
        if (tmp.length % 2 != 0) {
            throw new BadFormatException();
        }

        String[] data = new String[tmp.length / 2];
        for (int j = 0, i = 1; i < tmp.length; ++j, i += 2) {
            data[j] = tmp[i];
        }

        return data;
    }

    /**
     * Ottiene i byte dall'array di file allegati
     *
     * @param filesArray
     * @return
     * @throws IOException
     */
    private byte[] getByteFromArrayOfAttachedFiles(String[] filesArray) throws IOException {
        // TODO per inviare molteplici allegati creare un archivio compresso ed inviare solo l'archivio
        byte[] toSend = null;
        for (String s : filesArray) {
            File file = new File(s);
            FileInputStream fileInputStream = null;

            try {
                fileInputStream = new FileInputStream(file);
                toSend = IOUtils.toByteArray(fileInputStream);
            } finally {
                try {
                    if (fileInputStream != null)
                        fileInputStream.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        return toSend;
    }


    private String savedFiles(String receivedFile, String username) throws FileUploadException {
        try {
            String[] fileToSave = getDataFromString(receivedFile);
            List<String> savedFiles = saveAttachedFiles(username, fileToSave);
            if (savedFiles != null) {
                StringBuilder attachedFiles = new StringBuilder();
                savedFiles.forEach(f -> attachedFiles.append(f).append(";"));
                return attachedFiles.toString();
            }
        } catch (SecurityException | IOException e) {
            throw new FileUploadException("Error saving attached files");
        } catch (BadFormatException e) {
            System.out.println("No attached files found");
        }

        return null;
    }


    /**
     * N.B. Viene invocata quando un ticket passa da VALIDATION a PENDING.
     * Metodo per spostare il ticket tra gli stati della FSM.
     *
     * @param ticketID ID del ticket di cui deve essere cambiato lo stato
     * @param action Azione che attiva la transizione di stato
     * @param internalUserID ID del nuovo ResolverUser
     * @return il ticket aggiornato.
     */
    @RequestMapping(path = "/changeState/{ticketID}/{action}/{internalUserID}",method = RequestMethod.POST)
    public ResponseEntity<Ticket> changeTicketStateAndResolverUser(@PathVariable("ticketID") Long ticketID, @PathVariable("action") String action,
                                                                   @PathVariable("internalUserID") Long internalUserID){
        Ticket updatedTicket = null;
        try {
            updatedTicket = ticketController.changeStatusAndAssignee(ticketID,action,internalUserID);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if(updatedTicket != null)
            return new ResponseEntity<>(updatedTicket,HttpStatus.OK);
        else
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Ottiene il GANTT e avanza lo stato del ticket
     *
     * @param ticketID id del ticket da modificare
     * @param action
     * @param internalUserID id dell'utente a cui assegnare il ticket
     * @param duration
     * @param firstDay
     * @param username
     * @param ticket
     * @return
     */
    @RequestMapping(path = "/getPlanningAndChangeTicketState/{ticketID}/{action}/{internalUserID}/{duration}/{date}/{username}",method = RequestMethod.POST)
    public ResponseEntity<List<GanttDay>> getPlanningAndChangeTicketState(@PathVariable("ticketID") Long ticketID, @PathVariable("action") String action,
                                                                   @PathVariable("internalUserID") Long internalUserID,
                                                                  @PathVariable("duration") Integer duration,
                                                                  @PathVariable("date") String firstDay, @PathVariable("username") String username,
                                                                  @RequestBody Ticket ticket){
        List<GanttDay> ganttDays = null;
        try {
            try {
                ganttDays = ticketController.getPlanningAndChangeTicketState(ticket, username, firstDay, duration, ticketID, action, internalUserID);
            } catch (DependeciesFoundException e) {
                return new ResponseEntity<>(HttpStatus.FAILED_DEPENDENCY);
            }
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!ganttDays.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        return new ResponseEntity<>(ganttDays, HttpStatus.OK);
    }


    /**
     * Restituisce i file allegati al ticket con id specificato
     *
     * @param id del ticket da cui recuperare gli allegati
     * @return list of file bytes
     * @throws FileUploadException
     * @throws EntityNotFoundException
     */
    @GetMapping(value = "attached/{id}", produces = MediaType.ALL_VALUE)
    @ResponseStatus(OK)
    public byte[] getAttachedFiles(@PathVariable Long id) throws FileUploadException, EntityNotFoundException {
        Ticket ticket = ticketController.getTicketById(id);
        if (!canAccessTo(ticket, getCurrentUserFromSecurityContext())) {
            throw new TicketPrivateException(String.format(
                    "L'assistant corrente non può accedere al ticket %d perché è un ticket privato",
                    ticket.getId()
            ));
        }

        String files = ticket.getAttachments();
        String[] filesArray = files.split(";");

        try {
            return getByteFromArrayOfAttachedFiles(filesArray);
        } catch (IOException e) {
            throw new FileUploadException("Attached file non trovato");
        }
    }


    /**
     * Metodo per spostare il ticket tra gli stati della FSM.
     *
     * @param ticketID ID del ticket di cui deve essere cambiato lo stato
     * @param action Azione che attiva la transizione di stato
     * @return il ticket aggiornato.
     */
    @RequestMapping(path = "/changeState/{ticketID}/{action}",method = RequestMethod.POST)
    public ResponseEntity<Ticket> changeTicketState(@PathVariable("ticketID") Long ticketID, @PathVariable("action") String action){
        Ticket updatedTicket = null;
        try {
            updatedTicket = ticketController.changeStatus(ticketID,action);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if(updatedTicket != null)
            return new ResponseEntity<>(updatedTicket,HttpStatus.OK);
        else
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // ________________________________________________________

    /**
     * Metodo che gestisce una richiesta per ottenere id e nomi di tutti i ticket equivanenti a quello
     * avente id specificato per parametro.
     * @param ticketId Id del ticket di cui ricercare quelli equivalenti
     * @return Lista di stringhe contenenti Id e nomi dei ticket equivalenti
     */
    @RequestMapping(path = "/relation/equivalence/{ticketId}",method = RequestMethod.GET)
    public ResponseEntity getEquivalentTicketTitles(@PathVariable("ticketId") Long ticketId){

        List<String> ticketTitles = ticketController.getEquivalentTickets(ticketId);
        return new ResponseEntityBuilder<>(ticketTitles).setStatus(HttpStatus.OK).build();
    }

    /**
     * Metodo per modificare la descrizione del ticket.
     *
     * @param id ID del ticket di cui deve essere cambiato lo stato
     * @param description nuova descrizione
     * @return il ticket aggiornato.
     */
    @JsonView(JsonViews.DetailedTicket.class)
    @RequestMapping(path = "upd/{id}/{description}", method = RequestMethod.POST)
    public ResponseEntity upd(@PathVariable("id") Long id, @PathVariable("description") String description) {

        System.out.println("ciao:");

        Ticket updatedTicket = null;

        try {
            updatedTicket = ticketController.changeDescription(id, description);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if(updatedTicket != null)
            return new ResponseEntity<>(updatedTicket,HttpStatus.OK);
        else
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

    }

    /**
     * Metodo usato per la gestione di una GET che arriva sull'url specificato. A fronte di
     * una richiesta di questo tipo viene restituito il ticket che ha l'id specificato.
     *
     * @param id ID del ticket da restituire.
     * @return Ticket presente nel DB che risponde ai criteri sopraspecificati + esito della richiesta HTTP.
     * @see com.isssr.ticketing_system.controller.TicketController
     */
    @JsonView(JsonViews.Basic.class)
    @RequestMapping(path = "getTicketById2/{id}", method = RequestMethod.GET)
    public ResponseEntity getTicketById2(@PathVariable Long id) {

        Ticket ticket;

        try {
            ticket = ticketController.getTicketById(id);
        } catch (EntityNotFoundException e) {
            return CommonResponseEntity.NotFoundResponseEntity("TICKET_NOT_FOUND");
        }

        return new ResponseEntityBuilder<>(ticket).setStatus(HttpStatus.OK).build();
    }
}
