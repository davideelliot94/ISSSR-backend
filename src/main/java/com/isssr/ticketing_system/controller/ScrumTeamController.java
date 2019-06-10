package com.isssr.ticketing_system.controller;

import com.isssr.ticketing_system.dao.ScrumTeamDao;
import com.isssr.ticketing_system.dao.TargetDao;
import com.isssr.ticketing_system.dao.UserDao;
import com.isssr.ticketing_system.dto.ScrumTeamDto;
import com.isssr.ticketing_system.entity.ScrumTeam;
import com.isssr.ticketing_system.entity.Target;
import com.isssr.ticketing_system.entity.User;
import com.isssr.ticketing_system.exception.EntityNotFoundException;
import com.isssr.ticketing_system.exception.InvalidScrumTeamException;
import com.isssr.ticketing_system.logger.aspect.LogOperation;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.isssr.ticketing_system.enumeration.UserRole.CUSTOMER;

@Service
public class ScrumTeamController {
    @Autowired
    private ScrumTeamDao scrumTeamDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private TargetDao targetDao;


    /**
     * Metodo usato per inserire uno scrum team nel DB.
     *
     * @param scrumTeamDto scrum team che va aggiunto al DB.
     * @return info ddello scrum team aggiunto al DB
     */
    @Transactional
    //@LogOperation(tag = "SCRUM_TEAM_CREATE", inputArgs = {"team"})
    //@PreAuthorize("hasAuthority('ROLE_TEAM_COORDINATOR')")
    public ScrumTeamDto insertScrumTeam(ScrumTeamDto scrumTeamDto) throws InvalidScrumTeamException {
        Optional<User> foundUserPO = userDao.findById(scrumTeamDto.getProductOwner());
        if (!foundUserPO.isPresent()) {
            throw new InvalidScrumTeamException("");
        }
        User productOwner = foundUserPO.get();
        if (productOwner.getRole().equals(CUSTOMER)) {
            throw new InvalidScrumTeamException("");
        }

        Optional<User> foundUserSM = userDao.findById(scrumTeamDto.getScrumMaster());
        if (!foundUserSM.isPresent()) {
            throw new InvalidScrumTeamException("");
        }
        User scrumMaster = foundUserSM.get();
        if (scrumMaster.getRole().equals(CUSTOMER)) {
            throw new InvalidScrumTeamException("");
        }
        List<User> teamMembers = userDao.findByIdIn(scrumTeamDto.getTeamMembers());
        for (User member: teamMembers) {
            if (member.getRole().equals(CUSTOMER)) {
                throw new InvalidScrumTeamException("");
            }
        }


        ScrumTeam newScrumTeam = new ScrumTeam(scrumTeamDto.getName(), scrumMaster, productOwner, teamMembers);
        scrumTeamDao.save(newScrumTeam);
        return scrumTeamDto;

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
     * @param scrumTeam, team da modificare
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
     * @param scrumTeam, team da modificare
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

    @Transactional
    public ArrayList<ScrumTeam> getScrumTeamList() {

        return scrumTeamDao.getScrumTeamList();
    }

    @Transactional
    public ArrayList<User> getMembersBySTId(Long id) {

        return userDao.getMembersBySTId(id);
    }

    @Transactional
    public User getScrumMasterBySTId(Long id) {

        return userDao.getScrumMasterBySTId(id);
    }

    @Transactional
    public User getProductOwnerBySTId(Long id) {

        return userDao.getProductOwnerBySTId(id);
    }

    @Transactional
    public void assignProductToST(Long tid, Long pid) {

        Target target = targetDao.getOne(pid);
        target.setScrumTeam(scrumTeamDao.getOne(tid));
        targetDao.save(target);
    }

}
