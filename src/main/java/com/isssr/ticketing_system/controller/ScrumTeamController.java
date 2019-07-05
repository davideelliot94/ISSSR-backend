package com.isssr.ticketing_system.controller;

import com.isssr.ticketing_system.acl.groups.Group;
import com.isssr.ticketing_system.dao.*;
import com.isssr.ticketing_system.dto.ScrumAssignmentDto;
import com.isssr.ticketing_system.dto.ScrumProductWorkflowDto;
import com.isssr.ticketing_system.dto.ScrumTeamDto;
import com.isssr.ticketing_system.dto.UserDto;
import com.isssr.ticketing_system.entity.*;
import com.isssr.ticketing_system.exception.EntityNotFoundException;
import com.isssr.ticketing_system.exception.InvalidScrumTeamException;
import com.isssr.ticketing_system.exception.UndeletableScrumTeamException;
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
    @Autowired
    private ScrumProductWorkflowDao scrumProductWorkflowDao;
    @Autowired
    private SprintDao sprintDao;
    @Autowired
    private GroupDAO groupDAO;

    /**
     * Metodo usato per inserire uno scrum team nel DB.
     *
     * @param scrumTeamDto scrum team che va aggiunto al DB.
     * @return info ddello scrum team aggiunto al DB
     */
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ScrumTeamDto insertScrumTeam(ScrumTeamDto scrumTeamDto) throws InvalidScrumTeamException {
        Optional<User> foundUserPO = userDao.findById(scrumTeamDto.getProductOwner());
        if (!foundUserPO.isPresent()) {
            throw new InvalidScrumTeamException("");
        }
        User productOwner = foundUserPO.get();
        if (productOwner.getRole().equals(CUSTOMER)) {
            throw new InvalidScrumTeamException("");
        }
        assignScrumRoleIfNotOwned(productOwner);

        Optional<User> foundUserSM = userDao.findById(scrumTeamDto.getScrumMaster());
        if (!foundUserSM.isPresent()) {
            throw new InvalidScrumTeamException("");
        }
        User scrumMaster = foundUserSM.get();
        if (scrumMaster.getRole().equals(CUSTOMER)) {
            throw new InvalidScrumTeamException("");
        }
        assignScrumRoleIfNotOwned(scrumMaster);

        List<User> teamMembers = userDao.findByIdIn(scrumTeamDto.getTeamMembers());
        for (User member: teamMembers) {
            if (member.getRole().equals(CUSTOMER)) {
                throw new InvalidScrumTeamException("");
            }
            assignScrumRoleIfNotOwned(member);
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
    public List<ScrumTeam> getScrumTeamList() {
        List<ScrumTeam> scrumTeams = scrumTeamDao.findAll();
        return scrumTeams;
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
    public ScrumAssignmentDto assignProduct(Long scrumTeamId, Long productId, Long workflowId) {

        Target target = targetDao.getOne(productId);
        ScrumProductWorkflow scrumProductWorkflow = scrumProductWorkflowDao.getOne(workflowId);
        ScrumTeam scrumTeam = scrumTeamDao.getOne(scrumTeamId);
        target.setScrumProductWorkflow(scrumProductWorkflow);
        target.setScrumTeam(scrumTeam);
        targetDao.save(target);

        ScrumAssignmentDto scrumAssignmentDto = new ScrumAssignmentDto();
        scrumAssignmentDto.setProduct(target.getName());
        scrumAssignmentDto.setScrumTeam(scrumTeam.getName());
        scrumAssignmentDto.setScrumProductWorkflow(scrumProductWorkflow.getName());
        return scrumAssignmentDto;
    }

    /*Restituisce lo Scrum Team al lavoro sullo sprint avente l'id indicato*/
    public List<UserDto> findTeamBySprint(Long sprintId) throws EntityNotFoundException {
        Optional<Sprint> sprintSearchResult = sprintDao.findById(sprintId);
        if (!sprintSearchResult.isPresent()) {
            throw new EntityNotFoundException();
        }
        ScrumTeam scrumTeam = sprintSearchResult.get().getProduct().getScrumTeam();
        List<UserDto> teamMembers = new ArrayList<>();
        User scrumMaster = scrumTeam.getScrumMaster();
        ModelMapper modelMapper = new ModelMapper();
        UserDto scrumMasterDto = modelMapper.map(scrumMaster, UserDto.class);
        User productOwner = scrumTeam.getProductOwner();
        UserDto productOwnerDto = modelMapper.map(productOwner, UserDto.class);
        List<UserDto> memberDtos = new ArrayList<>();
        memberDtos.add(scrumMasterDto);
        memberDtos.add(productOwnerDto);
        for (User member : scrumTeam.getTeamMembers()) {
            memberDtos.add(modelMapper.map(member, UserDto.class));
        }
        return memberDtos;
    }

    private void assignScrumRoleIfNotOwned(User user){
        Group scrumGroup = groupDAO.findByName("GRUPPO SCRUM");
        List<User> owners = scrumGroup.getMembers();
        if (!owners.contains(user)){
            scrumGroup.getMembers().add(user);
            groupDAO.save(scrumGroup);
        }
    }
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void deleteScrumTeam(Long scrumTeamId) throws EntityNotFoundException, UndeletableScrumTeamException {
        Optional<ScrumTeam> scrumTeamSearchResult = scrumTeamDao.findById(scrumTeamId);
        if (!scrumTeamSearchResult.isPresent()) {
            throw new EntityNotFoundException();
        }
        ScrumTeam scrumTeamToDelete = scrumTeamSearchResult.get();
        List<Target> products = scrumTeamToDelete.getProducts();
        if (!products.isEmpty()) {
            throw new UndeletableScrumTeamException();
        }
        scrumTeamDao.delete(scrumTeamToDelete);
    }
}
