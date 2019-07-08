package com.isssr.ticketing_system.rest;

import com.fasterxml.jackson.annotation.JsonView;
import com.isssr.ticketing_system.acl.groups.Group;
import com.isssr.ticketing_system.controller.CompanyController;
import com.isssr.ticketing_system.controller.GroupController;
import com.isssr.ticketing_system.entity.Company;
import com.isssr.ticketing_system.entity.SoftDelete.SoftDelete;
import com.isssr.ticketing_system.entity.SoftDelete.SoftDeleteKind;
import com.isssr.ticketing_system.enumeration.UserRole;
import com.isssr.ticketing_system.exception.EntityNotFoundException;
import com.isssr.ticketing_system.entity.User;
import com.isssr.ticketing_system.exception.NotFoundEntityException;
import com.isssr.ticketing_system.logger.aspect.LogOperation;
import com.isssr.ticketing_system.response_entity.*;
import com.isssr.ticketing_system.controller.UserController;
import com.isssr.ticketing_system.validator.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.lang.String.valueOf;
import static org.springframework.http.HttpStatus.OK;

/**
 * Questa classe gestisce le richieste HTTP che giungono sul path specificato ("users")
 * attraverso i metodi definiti nella classe RegisteredUserController.
 */
@Validated
@RestController
@RequestMapping(path = "users")
@CrossOrigin("*")
@SoftDelete(SoftDeleteKind.NOT_DELETED)
public class UserRest {

    private UserController userController;
    private GroupController groupController;
    private UserValidator userValidator;
    private CompanyController companyController;

    @Autowired
    public UserRest(
            UserController userController,
            GroupController groupController,
            UserValidator userValidator,
            CompanyController companyController
    ) {
        this.userController = userController;
        this.groupController = groupController;
        this.userValidator = userValidator;
        this.companyController = companyController;
    }

    @JsonView(JsonViews.DetailedUser.class)
    @RequestMapping(path = "not_customer", method = RequestMethod.GET)
    public ResponseEntity<List<User>> getAllNotCustomer() {
        List<User> userNotCustomer;
        try {
            userNotCustomer = userController.findAllNotCustomer();
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(userNotCustomer, HttpStatus.OK);
    }

    /**
     * Configura un validator per gli oggetti di tipo User
     *
     * @param binder binder
     */
    @InitBinder
    public void setupBinder(WebDataBinder binder) {
        binder.addValidators(userValidator);
    }

    /**
     * Restituisce l'user corrente
     *
     * @param principal principal dell'utente che ha inviato la richiesta
     * @return utente corrente + esito della richiesta HTTP
     */
    @JsonView(JsonViews.DetailedUser.class)
    @RequestMapping(path = "self", method = RequestMethod.GET)
    public ResponseEntity<User> self(@AuthenticationPrincipal Principal principal) {
        User user;
        try {
            user = userController.findUserByUsername(principal.getName());
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    /**
     * Metodo usato per la gestione di una POST che arriva sull'url specificato. A fronte di
     * una richiesta di questo tipo l'utente viene inserito nel DB.
     *
     * @param user utente che va aggiunto al DB.
     * @return info dell'utente aggiunto al DB + esito della richiesta HTTP.
     * @see com.isssr.ticketing_system.controller.RegisteredUserController
     */
    @JsonView(JsonViews.DetailedUser.class)
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity create(@Valid @RequestBody User user) {
        if (userController.existsByUsername(user.getUsername()))
            return new ResponseEntity<>(HttpStatus.CONFLICT);

        User newUser = userController.insertUser(user);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }


    /**
     * Metodo usato per la gestione di una get che arriva sull'url specificato. A fronte di
     * una richiesta di questo tipo viene restituito l'utente che ha l'id specificato
     *
     * @param id id dell'utente cercato
     * @return utente cercato + esito della richiesta HTTP
     */
    @JsonView(JsonViews.DetailedUser.class)
    @RequestMapping(path = "{id}", method = RequestMethod.GET)
    public ResponseEntity getById(@PathVariable Long id, @AuthenticationPrincipal Principal principal) {
        User user;
        User currentUser;
        try {
            user = userController.findById(id);
            currentUser = userController.findUserByUsername(principal.getName());
            if(!id.equals(currentUser.getId()) && !valueOf(currentUser.getRole()).equals("ADMIN")) {
                System.err.println("Attack in while");
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(user, HttpStatus.OK);
    }


    /**
     * Metodo usato per la gestione di una PUT che arriva sull'url specificato. A fronte di
     * una richiesta di questo tipo le info aggiornate relative all'utente specificato
     * vengono inserite nel DB.
     *
     * @param id Id dell'utente da aggiornare.
     * @param user info aggiornate dell'utente.
     * @return utente eventualmente aggiornato + esito della richiesta HTTP.
     * @see com.isssr.ticketing_system.controller.RegisteredUserController
     */
    @JsonView(JsonViews.DetailedUser.class)
    @RequestMapping(path = "{id}", method = RequestMethod.PUT)
    public ResponseEntity update(@PathVariable Long id,
                                 @Valid @RequestBody User user) {
        User updatedUser;

        try {
            updatedUser = userController.updateById(id, user);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    /**
     * Metodo usato per la gestione di una Delete che arriva sull'url specificato. A fronte di
     * una richiesta di questo tipo l'utente specificato viene cancellato dal DB.
     *
     * @param id Id dell'utente che va cancellato dal DB.
     * @return utente cancellato dal DB + esito della richiesta HTTP.
     * @see com.isssr.ticketing_system.controller.RegisteredUserController
     */
    @JsonView(JsonViews.DetailedUser.class)
    @RequestMapping(path = "{id}", method = RequestMethod.DELETE)
    public ResponseEntity deleteUserById(@PathVariable Long id) {
        try {
            userController.deleteById(id);
        } catch (NotFoundEntityException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }



    /**
     * Metodo usato per la gestione di una get che arriva sull'url specificato. A fronte di
     * una richiesta di questo tipo vengono restituiti tutti gli utenti nel DB.
     *
     * @return utenti presenti nel DB + esito della richiesta HTTP
     */
    @JsonView(JsonViews.DetailedUser.class)
    @RequestMapping(path="", method = RequestMethod.GET)
    public ResponseEntity getAllUsers() {
        List<User> users = userController.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    /**
     * Metodo usato per la gestione di una Delete che arriva sull'url specificato. A fronte di
     * una richiesta di questo tipo tutti gli utenti presenti nel sistema vengono eliminati
     *
     * @return esito della richiesta HTTP.
     * @see com.isssr.ticketing_system.controller.UserController
     */
    @JsonView(JsonViews.DetailedUser.class)
    @RequestMapping(method = RequestMethod.DELETE)
    public ResponseEntity deleteAllUsers() {
        Long count = userController.count();

        if (count == 0)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        userController.deleteAll();

        return new ResponseEntity<>(HttpStatus.OK);
    }


    /**
     * Metodo usato per recuperare i metadati di interesse per un oggetto User
     *
     * @return HashMap contenente i metadati richiesti
     */
    @JsonView(JsonViews.Basic.class)
    @RequestMapping(path = "metadata", method = RequestMethod.GET)
    public ResponseEntity getMetadata() {

        Iterable<UserRole> roles = Arrays.asList(UserRole.values());
        Iterable<Company> companies = companyController.findAll();

        return new HashMapResponseEntityBuilder(HttpStatus.OK)
                .set("roles", roles)
                .set("companies", StreamSupport.stream(companies.spliterator(), false).collect(Collectors.toList()))
                .build();
    }


    /**
     * Restituisce tutti i gruppi di cui un utente è membro
     *
     * @param id id dell'utente su cui effettuare il controllo
     * @return lista di gruppi che soddisfa i criteri sopraindicati + esito della richiesta HTTP
     */
    @RequestMapping(path = "{id}/groups", method = RequestMethod.GET)
    @ResponseStatus(OK)
    public List<Group> getGroupsByUser(@PathVariable Long id) {
        User u = userController.getUser(id);
        return groupController.getGroupsByMember(u);
    }


    /**
     * Metodo che restituisce tutti gli User impiegati in base al loro ruolo.
     *
     * @param role ruolo da cercare nel sistema
     * @return lista di InternalUser
     */
    @RequestMapping(path= "getEmployedUserByRole/{role}", method = RequestMethod.GET)
    public ResponseEntity<List<? extends User>> getEmployedUserByRole(@PathVariable UserRole role)
    {
        List<? extends User> listInternalUser = userController.getEmployedUserByRole(role);
        if(listInternalUser!= null)
            return new ResponseEntity<>(listInternalUser, HttpStatus.OK);
        return new ResponseEntity<>(listInternalUser, HttpStatus.NOT_FOUND);

    }


    /**
     * Metodo usato per ottenere un TeamCoordinator estratto a caso dal DB
     *
     * @return TeamCoordinator casuale
     */
    @RequestMapping(path = "team_coordinator", method = RequestMethod.GET)
    public ResponseEntity<User> getTeamCoordinator(){
        User teamCoordinator = userController.getTeamCoordinator();
        if(teamCoordinator != null)
            return new ResponseEntity<>(teamCoordinator, HttpStatus.OK);
        return new ResponseEntity<>(teamCoordinator, HttpStatus.NOT_FOUND);

    }

    @JsonView(JsonViews.Basic.class)
    @RequestMapping(value = "insertUserInGroup/{idu}/{role}", method = RequestMethod.POST)
    @ResponseStatus(OK)
    //@LogOperation(inputArgs = {"idu, role"}, returnObject = false, tag = "insert_user_in_group", opName = "insertUserInGroup")
    public Group insertUserInGroup(@PathVariable Long idu, @PathVariable String role) {


        Long idg = groupController.getGroupByRole("%"+role+"%");

        Group group = groupController.getGroup(idg);

        List<User> list = new ArrayList<>();
        User user = new User();
        user.setId(idu);
        list.add(user);

        group.setUsers(list);

        groupController.saveGroup(group);

        return group;

    }

    @GetMapping("getMaxId")
    @ResponseStatus(OK)
    public Long getMaxId() {

        return userController.getMaxId();

    }

}
