package com.isssr.ticketing_system.controller;

import com.isssr.ticketing_system.acl.defaultpermission.TeamDefaultPermission;
import com.isssr.ticketing_system.dao.UserDao;
import com.isssr.ticketing_system.entity.User;
import com.isssr.ticketing_system.exception.EntityNotFoundException;
import com.isssr.ticketing_system.logger.aspect.LogOperation;
import com.isssr.ticketing_system.entity.SoftDelete.SoftDelete;
import com.isssr.ticketing_system.entity.SoftDelete.SoftDeleteKind;
import com.isssr.ticketing_system.entity.Team;
import com.isssr.ticketing_system.dao.TeamDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * Questa classe definisce metodi che si interfacciano con la classe TeamDao per l'interazione con il DB.
 */
@Service
@SoftDelete(SoftDeleteKind.NOT_DELETED)
public class TeamController {

    private TeamDao teamDao;
    private UserDao userDao;
    private TeamDefaultPermission teamDefaultPermission;
    private UserController userController;

    @Autowired
    public TeamController(
            TeamDao teamDao,
            TeamDefaultPermission teamDefaultPermission,
            UserController userController,
            UserDao userDao
    ) {
        this.teamDao = teamDao;
        this.teamDefaultPermission = teamDefaultPermission;
        this.userController = userController;
        this.userDao = userDao;
    }


    /**
     * Metodo usato per inserire un team nel DB.
     *
     * @param team team che va aggiunto al DB.
     * @return info del team aggiunto al DB
     */
    @Transactional
    @LogOperation(tag = "TEAM_CREATE", inputArgs = {"team"})
    @PreAuthorize("hasAuthority('ROLE_TEAM_COORDINATOR')")
    public Team insertTeam(Team team) {
        Team newTeam = this.teamDao.save(team);
        teamDefaultPermission.grantDefaultPermission(team.getId());
        teamDefaultPermission.denyDefaultPermission(team.getId());

        return newTeam;
    }

    /**
     * Metodo usato per aggiornare il team specificato
     *
     * @param id id del team che deve essere aggiornato
     * @param updatedTeam team con i campi aggiornati
     * @return team aggiornato
     */
    @Transactional
    @LogOperation(tag = "TEAM_UPDATED", inputArgs = {"team"})
    @PreAuthorize("hasPermission(#id,'com.uniroma2.isssrbackend.entity.Team','WRITE') or hasAuthority('ROLE_ADMIN')")
    public Team updateTeamById(@NotNull Long id, @NotNull Team updatedTeam) throws EntityNotFoundException {

        Optional<Team> toBeUpdatedTeam = teamDao.findById(id);
        if (!toBeUpdatedTeam.isPresent())
            throw new EntityNotFoundException("Team to update not found in DB, maybe you have to create a new one");

        toBeUpdatedTeam.get().updateTeam(updatedTeam);
        return teamDao.save(toBeUpdatedTeam.get());
    }

    /**
     * Restituisce il team che ha l'id specificato
     *
     * @param id id del team richiesto
     * @return team cercato
     */
    @Transactional
    @PostAuthorize("hasPermission(returnObject,'READ') or returnObject==null  or hasAuthority('ROLE_ADMIN')")
    public Team getTeamById(Long id) throws EntityNotFoundException {
        Optional<Team> team = this.teamDao.findById(id);

        if (!team.isPresent()) {
            throw new EntityNotFoundException("TEAM NOT FOUND");
        }

        return team.get();
    }

    /**
     * Restituisce il team che ha il nome specificato.
     *
     * @param name nome del team cercato
     * @return team cercato
     */
    @Transactional
    @PostAuthorize("hasPermission(returnObject,'READ') or returnObject==null  or hasAuthority('ROLE_ADMIN')")
    public Team findByName(String name) throws EntityNotFoundException {
        Optional<Team> team = this.teamDao.findByName(name);

        if (!team.isPresent()) {
            throw new EntityNotFoundException("TEAM NOT FOUND");
        }

        return team.get();
    }

    /**
     * Verifica se il team che ha l'id specificato è presente nel DB.
     *
     * @param id id del team su cui effettuare il controllo
     * @return true se il team cercato esiste, false altrimenti
     */
    @Transactional
    public boolean existsById(Long id) {
        return this.teamDao.existsById(id);
    }


    /**
     * Restituisce tutti i team presenti nel DB.
     *
     * @return lista dei team
     */
    @Transactional
    @PostFilter("hasPermission(filterObject,'READ') " +
            "or hasAuthority('ROLE_TEAM_COORDINATOR')  or hasAuthority('ROLE_ADMIN')")
    public Iterable<Team> findAll() {
        return this.teamDao.findAll();
    }

    /**
     * Restituisce i team corrispondenti agli id specificati
     *
     * @param ids lista di id dei team ceracti
     * @return lista dei team cercati
     */
    @Transactional
    @PostFilter("hasPermission(filterObject,'READ') " +
            "or hasAuthority('ROLE_TEAM_COORDINATOR')  or hasAuthority('ROLE_ADMIN')")
    public Iterable<Team> findAllById(Iterable<Long> ids) {
        return this.teamDao.findAllById(ids);
    }

    /**
     * Restituisce il numero di team presenti nel DB.
     *
     * @return numero di team presenti nel DB.
     */
    @Transactional
    public long count() {
        return this.teamDao.count();
    }

    /**
     * Elimina il team che ha l'id specificato
     *
     * @param id id del team da eliminare
     * @return true se il team è stato cancellato, false altrimenti
     */
    @Transactional
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TEAM_COORDINATOR')")
    public boolean deleteTeamById(Long id) throws EntityNotFoundException {
        Team team = getTeamById(id);
        team.delete();
        this.teamDao.save(team);
        teamDefaultPermission.removeDefaultPermission(id);

        return true;
    }


    /**
     * Elimina tutti i team presenti nel DB.
     *
     */
    @Transactional
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TEAM_COORDINATOR')")
    public void deleteAll() {
        this.teamDao.deleteAll();
    }

    /**
     * Verifica se il team che ha il nome specificato esiste nel DB.
     *
     * @param name nome del team cercato
     * @return true se il team cercato esiste, false altrimenti
     */
    @Transactional
    public boolean existsByName(String name) {
        return this.teamDao.existsByName(name);
    }

    /**
     * Restituisce tutti i team presenti nel sistema.
     *
     * @return lista dei team presenti nel sistema.
     */
    @Transactional
    @PostFilter("hasPermission(filterObject,'READ') " +
            "or hasAuthority('ROLE_TEAM_COORDINATOR')  or hasAuthority('ROLE_ADMIN')")
    public List<Team> getAllTeams() {
        return this.teamDao.findAll();
    }


    /**
     * Restituisce i team member del team con id specificato
     *
     * @param id id del team cercato
     * @return collection di utenti membri del team specificato
     */
    public Collection<User> getTeamMembersByTeamId(@NotNull Long id){
        return teamDao.getTeamMembersByTeamId(id);
    }

    /**
     * Restituisce tutti i team members corrispondenti a un dato team leader
     *
     * @param id del team leader
     * @return collection dei team members
     */
    public Collection<User> getTeamMembersByTeamLeaderId(@NotNull Long id){
        return teamDao.getTeamMembersByTeamLeaderId(id);
    }

    /**
     * Metodo usato per aggiungere un team member a un team specificato
     *
     * @param teamID id del team da modificare
     * @param teamMemberID id del team member da aggiungere al team
     * @return team aggiornato
     */
    public Team addTeamMember(@NotNull Long teamID, Long teamMemberID) throws EntityNotFoundException {
        Team teamToUpdate = teamDao.getOne(teamID);
        User teamMember = userController.findById(teamMemberID);

        teamToUpdate.addMember(teamMember);
        return teamDao.save(teamToUpdate);
    }

    /**
     * Restituisce il team leader del team specificato
     *
     * @param id del team
     * @return team leader
     */
    public User getTeamLeaderByTeamId(Long id) {
        Team team = teamDao.getOne(id);
        return team.getTeamLeader();

    }

    /**
     * Setta il team leader del team specificato
     *
     * @param team team da modificare
     * @param assistant team leader da aggiungere al team
     * @return team modificato
     */
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_TEAM_COORDINATOR')  or hasAuthority('ROLE_ADMIN')")
    @LogOperation(inputArgs = {"team,assistant"}, tag = "team_leader", opName = "setTeamLeader")
    public Team setTeamLeader(Team team, User assistant) {
        team.setTeamLeader(assistant);
        teamDao.saveAndFlush(team);
        return team;
    }

    /**
     * Metodo usato per aggiungere una lista di team members a un team
     *
     * @param team team da modificare
     * @param assistants team members da agigungere al team specificato
     * @return team modificato
     */
    @Transactional
    @PreAuthorize("hasAnyAuthority('ROLE_TEAM_COORDINATOR','ROLE_ADMIN')")
    @LogOperation(inputArgs = {"team,assistants"}, tag = "team_assistants", opName = "addAssistantsToTeam")
    public Team addAssistantsToTeam(Team team, List<User> assistants) {
        team.addUsers(assistants);
        teamDao.saveAndFlush(team);
        return team;
    }


    /**
     * Restituisce tutti i team a cui un utente appartiene
     *
     * @param username nome dell'utente cercato
     * @return lista dei team
     */
    @Transactional
    @SoftDelete(SoftDeleteKind.NOT_DELETED)
    public List<Team> findAllTeamByPerson(String username) throws EntityNotFoundException {
        User user = userController.findUserByUsername(username);
        HashSet<User> set = new HashSet<>();
        set.add(user);
        return  teamDao.findAllByTeamMembersContainsOrTeamLeaderOrTeamCoordinator(set,user,user);
    }
}
