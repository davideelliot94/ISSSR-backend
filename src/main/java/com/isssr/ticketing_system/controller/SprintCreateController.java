package com.isssr.ticketing_system.controller;

import com.isssr.ticketing_system.dao.*;
import com.isssr.ticketing_system.dto.SprintDTO;
import com.isssr.ticketing_system.dto.SprintWithUserRoleDto;
import com.isssr.ticketing_system.entity.ScrumTeam;
import com.isssr.ticketing_system.entity.Sprint;
import com.isssr.ticketing_system.entity.Target;
import com.isssr.ticketing_system.entity.User;
import com.isssr.ticketing_system.entity.*;
import com.isssr.ticketing_system.enumeration.TicketPriority;
import com.isssr.ticketing_system.exception.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sun.reflect.generics.tree.TypeArgument;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class SprintCreateController {

    private static final int MAX_SPRINT_DURATION = 4;   //TODO MOVE IN PROPERTIES

    @Autowired
    private ScrumTeamDao scrumTeamDao;
    @Autowired
    private TargetDao targetDao;

    @Autowired
    private UserDao userDao;


    @Autowired
    private SprintDao sprintDao;

    @Autowired
    private BacklogItemDao backlogItemDao;

    @Transactional
    @PreAuthorize("hasAnyAuthority('ROLE_SCRUM', 'ROLE_ADMIN')")
    public void insertSprint(SprintDTO sprintDTO,String username) throws UnauthorizedUserException,IllegalArgumentException {
        //sprint check correctness
        int duration=sprintDTO.getDuration();
        if(duration<0 || duration>MAX_SPRINT_DURATION){
            throw  new IllegalArgumentException("DURATION INVALID");
        }
        //sprintDTO convert to entity
        Target relatedTarget = targetDao.findById(sprintDTO.getIdProduct()).get();

        //CHECK IF CURRENT USER IS SCRUM MASTER OF PRODUCT RELATED TO THE SPRINT IN CREATION
        if(!relatedTarget.getScrumTeam().getScrumMaster().getUsername().equals(username)){
            throw new UnauthorizedUserException("unautorized to create sprint for "+username);
        }
        Sprint sprint = new Sprint();
        Sprint currentSprintNum=sprintDao.findFirstByProductOrderByNumberDesc(relatedTarget);
        Integer nextSprintNumber = currentSprintNum==null?1:currentSprintNum.getNumber()+1;
        sprint.setNumber(nextSprintNumber);
        sprint.setDuration(sprintDTO.getDuration());
        sprint.setProduct(relatedTarget);
        sprint.setSprintGoal(sprintDTO.getSprintGoal());
        sprint.setIsActive(null);
        LocalDate date =  LocalDate.now();
        sprint.setStartDate(date);

        sprintDao.save(sprint);

    }

    /*
    cerco gli scrumTeam in cui l'utente è product owner
    e ricavo la ista di prodotti associati a quel team
    e da li la lista di sprint.
     */

    @Transactional
    public List<SprintDTO> getSprintsByPO(Long idPO) {
         Optional<User> productOwner = this.userDao.findById(idPO);
         List<ScrumTeam> scrumTeams = this.scrumTeamDao.findAllByProductOwner(productOwner.get());
         List<Sprint> sprints = new ArrayList<>();
         for( ScrumTeam team : scrumTeams) {
             for(Target product : team.getProducts()) {
                 sprints.addAll(product.getSprints());
             }
         }
        //conversione in SprintDTO
        List<SprintDTO> sprintDTOs = new ArrayList<>();
         for(Sprint sprint : sprints) {
             ModelMapper modelMapper = new ModelMapper();
             SprintDTO sprintDTO = modelMapper.map(sprint, SprintDTO.class);

             //TODO inserire anche il PO e SM dello Scrum Team??
             sprintDTOs.add(sprintDTO);
         }
         return sprintDTOs;

    }

    /* Restituisce gli Sprint associati ai prodotti su cui il teamMember lavora o sta lavorando*/
    public List<SprintWithUserRoleDto> getSprintsByScrumTeamMember(Long teamMemberId) throws EntityNotFoundException {
       // Ricerca dell'utente
        Optional<User> userSearchResult = userDao.findById(teamMemberId);
       if (!userSearchResult.isPresent()) {
           throw new EntityNotFoundException();
       }

       List<SprintWithUserRoleDto> sprintDTOs = new ArrayList<>();

       // Ricerca degli Scrum Team a cui appartiene l'utente
        List<ScrumTeam> userScrumTeams = new ArrayList<>();
        List<ScrumTeam> tempScrumTeams = scrumTeamDao.findAllByProductOwner(userSearchResult.get());
        if (tempScrumTeams != null) {
            userScrumTeams.addAll(tempScrumTeams);
        }

        // Ricerco gli sprint associati ai prodotti associati ai team. Per ciascuno creo il
        // DTO impostando isProductOwner a true.
        List<Sprint> sprints = new ArrayList<>();
        for (ScrumTeam team: tempScrumTeams) {
            for (Target product : team.getProducts()) {
                sprints.addAll(product.getSprints());
            }
        }
        for (Sprint sprint : sprints) {
            ModelMapper modelMapper = new ModelMapper();
            SprintWithUserRoleDto sprintDTO = modelMapper.map(sprint, SprintWithUserRoleDto.class);
            sprintDTO.setIsUserProductOwner(true);
            sprintDTOs.add(sprintDTO);
        }

        tempScrumTeams = scrumTeamDao.findAllByScrumMaster(userSearchResult.get());
        if (tempScrumTeams != null) {
            userScrumTeams.addAll(tempScrumTeams);
        }

        // Ricerco gli sprint associati ai prodotti associati ai team. Per ciascuno creo il
        // DTO impostando isScrumMaster a true.
        sprints = new ArrayList<>();
        for (ScrumTeam team: tempScrumTeams) {
            for (Target product : team.getProducts()) {
                sprints.addAll(product.getSprints());
            }
        }
        for (Sprint sprint : sprints) {
            ModelMapper modelMapper = new ModelMapper();
            SprintWithUserRoleDto sprintDTO = modelMapper.map(sprint, SprintWithUserRoleDto.class);
            sprintDTO.setIsUserScrumMaster(true);
            sprintDTOs.add(sprintDTO);
        }

        tempScrumTeams = scrumTeamDao.findAllByTeamMembersContains(userSearchResult.get());
        if (tempScrumTeams != null) {
            userScrumTeams.addAll(tempScrumTeams);
        }
        // ottengo gli sprint su cui lavorano gli Scrum Teams
        sprints = new ArrayList<>();
        for(ScrumTeam team : tempScrumTeams) {
            for(Target product : team.getProducts()) {
                sprints.addAll(product.getSprints());
            }
        }
        for (Sprint sprint : sprints) {
            ModelMapper modelMapper = new ModelMapper();
            SprintWithUserRoleDto sprintDTO = modelMapper.map(sprint, SprintWithUserRoleDto.class);
            sprintDTOs.add(sprintDTO);
        }
        return sprintDTOs;
    }

    public List<SprintDTO> getAllByProduct(Long productId) throws EntityNotFoundException {
        Optional<Target> target = targetDao.findById(productId);
        if (!target.isPresent()){
            throw new EntityNotFoundException();
        }
        List<Sprint> sprints = target.get().getSprints();
        List<SprintDTO> sprintDTOs = new ArrayList<>();
        ModelMapper modelMapper = new ModelMapper();
        for (Sprint sprint : sprints){
            SprintDTO sprintDTO = modelMapper.map(sprint, SprintDTO.class);
            sprintDTOs.add(sprintDTO);
        }
        return sprintDTOs;
    }

    @Transactional
    public void closeSprint(Long sprintId) {

        Sprint sprint = sprintDao.getOne(sprintId);
        int maxPriority = backlogItemDao.getMaxPriority(sprint.getProduct().getId());
        maxPriority++;

        List<BacklogItem> backlogItems = backlogItemDao.findBacklogItemBySprint(sprint);
        for (BacklogItem backlogItem : backlogItems){
            if (!backlogItem.getStatus().contains("Completato")){
                backlogItem.setStatus("");
                backlogItem.setSprint(null);
                backlogItem.setPriority(maxPriority);
                backlogItemDao.save(backlogItem);
            }
        }
        LocalDate date = LocalDate.now();
        sprint.setEndDate(date);
        sprint.setIsActive(false);
        sprintDao.save(sprint);

    }

    public List<String> getDates(Long sprintId) throws EntityNotFoundException {

        Sprint sprint = sprintDao.getOne(sprintId);
        LocalDate date = sprint.getStartDate();
        int duration = sprint.getDuration() * 7;

        List<String> dates = new ArrayList<>();
        dates.add("");
        dates.add(String.valueOf(date));

        for (int i = 2; i <= duration; i++) {

            dates.add(String.valueOf(date.plusDays(1)));
            date = date.plusDays(1);

        }

        return dates;

    }

    public void activateSprint(Long sprintId) throws EntityNotFoundException {
        Optional<Sprint> sprint = sprintDao.findById(sprintId);
        if (!sprint.isPresent()){
            throw new EntityNotFoundException();
        }
        sprint.get().setIsActive(true);
        sprintDao.save(sprint.get());
    }
}
