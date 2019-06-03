package com.isssr.ticketing_system.controller;

import com.isssr.ticketing_system.dao.ScrumTeamDao;
import com.isssr.ticketing_system.dao.UserDao;
import com.isssr.ticketing_system.entity.ScrumTeam;
import com.isssr.ticketing_system.entity.User;
import com.isssr.ticketing_system.exception.EntityNotFoundException;
import com.isssr.ticketing_system.exception.InvalidScrumTeamException;
import com.isssr.ticketing_system.logger.aspect.LogOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@Service
public class ScrumTeamController {
    private ScrumTeamDao scrumTeamDao;
    private UserDao userDao;

    @Autowired
    public ScrumTeamController(ScrumTeamDao scrumTeamDao, UserDao userDao) {
        this.scrumTeamDao = scrumTeamDao;
        this.userDao = userDao;
    }

    /**
     * Metodo usato per inserire uno scrum team nel DB.
     *
     * @param scrumTeam scrum team che va aggiunto al DB.
     * @return info dello scrum team aggiunto al DB
     */
    @Transactional
    //@LogOperation(tag = "TEAM_CREATE", inputArgs = {"team"})
    //@PreAuthorize("hasAuthority('ROLE_TEAM_COORDINATOR')")
    public ScrumTeam insertScrumTeam(ScrumTeam scrumTeam) throws InvalidScrumTeamException {
        int numberOfUsersNotCustomer = userDao.countUserNotCustomer();
        if (numberOfUsersNotCustomer > 2) {
            ScrumTeam newScrumTeam = this.scrumTeamDao.save(scrumTeam);
            //teamDefaultPermission.grantDefaultPermission(team.getId());
            //teamDefaultPermission.denyDefaultPermission(team.getId());
            return newScrumTeam;
        } else {
            throw new InvalidScrumTeamException("There must be al least three not customer users.");
        }
    }


    /**
     * Restituisce lo scrum team che ha l'id specificato
     *
     * @param id dello scrum team richiesto
     * @return scrum team cercato
     */
    @Transactional
    //@PostAuthorize("hasPermission(returnObject,'READ') or returnObject==null  or hasAuthority('ROLE_ADMIN')")
    public ScrumTeam getScrumTeamById(Long id) throws EntityNotFoundException {
        Optional<ScrumTeam> scrumTeam = this.scrumTeamDao.findById(id);

        if (!scrumTeam.isPresent()) {
            throw new EntityNotFoundException("SCRUM TEAM NOT FOUND");
        }

        return scrumTeam.get();
    }

    /**
     * imposta lo scrum master dello scrum team
     *
     * @param scrumteam, team da modificare
     * @param scrumMaster, scrum master da aggiungere al team
     * @return team modificato
     */
    @Transactional
    //@PreAuthorize("hasAuthority('ROLE_TEAM_COORDINATOR')  or hasAuthority('ROLE_ADMIN')")
    //@LogOperation(inputArgs = {"team,assistant"}, tag = "team_leader", opName = "setTeamLeader")
    public ScrumTeam setScrumMaster(ScrumTeam scrumTeam, User scrumMaster) {
        scrumTeam.setScrumMaster(scrumMaster);
        scrumTeamDao.saveAndFlush(scrumTeam);
        return scrumTeam;
    }

    /**
     * imposta il product owner dello scrum team
     *
     * @param scrumteam, team da modificare
     * @param productOwner, productOwner da aggiungere al team
     * @return team modificato
     */
    @Transactional
    //@PreAuthorize("hasAuthority('ROLE_TEAM_COORDINATOR')  or hasAuthority('ROLE_ADMIN')")
    //@LogOperation(inputArgs = {"team,assistant"}, tag = "team_leader", opName = "setTeamLeader")
    public ScrumTeam setProductOwner(ScrumTeam scrumTeam, User productOwner) {
        scrumTeam.setProductOwner(productOwner);
        scrumTeamDao.saveAndFlush(scrumTeam);
        return scrumTeam;
    }

    /**
     * Metodo usato per aggiungere una lista di team members ad uno scrum team
     *
     * @param scrumTeam, team da modificare
     * @param teamMembers, team members da agigungere al team specificato
     * @return team modificato
     */
    @Transactional
    //@PreAuthorize("hasAnyAuthority('ROLE_TEAM_COORDINATOR','ROLE_ADMIN')")
    //@LogOperation(inputArgs = {"team,assistants"}, tag = "team_assistants", opName = "addAssistantsToTeam")
    public ScrumTeam addTeamMembersToTeam(ScrumTeam scrumTeam, List<User> teamMembers) {
        scrumTeam.addUsers(teamMembers);
        scrumTeamDao.saveAndFlush(scrumTeam);
        return scrumTeam;
    }

}
