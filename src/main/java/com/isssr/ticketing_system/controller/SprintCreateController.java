package com.isssr.ticketing_system.controller;

import com.isssr.ticketing_system.dao.ScrumTeamDao;
import com.isssr.ticketing_system.dao.SprintDao;
import com.isssr.ticketing_system.dao.TargetDao;
import com.isssr.ticketing_system.dao.UserDao;
import com.isssr.ticketing_system.dto.SprintDTO;
import com.isssr.ticketing_system.dto.SprintWithUserRoleDto;
import com.isssr.ticketing_system.entity.ScrumTeam;
import com.isssr.ticketing_system.entity.Sprint;
import com.isssr.ticketing_system.entity.Target;
import com.isssr.ticketing_system.entity.User;
import com.isssr.ticketing_system.exception.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

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

    @Transactional
//    @LogOperation(tag = "SPRINT_CREATE", inputArgs = {"sprint"}, jsonView = JsonViews.DetailedSprint.class) //TODO ???
//    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_TEAM_MEMBER', 'ROLE_ADMIN')")       //TODO ROLE PRODUCT OWNER
    public void insertSprint(SprintDTO sprintDTO) {
        //sprint check correctness
        int duration=sprintDTO.getDuration();
        if(duration<0 || duration>MAX_SPRINT_DURATION){
            throw  new IllegalArgumentException("DURATION INVALID");
        }
        //sprintDTO convert to entity
        Target relatedTarget = targetDao.findById(sprintDTO.getIdProduct()).get();
//        ModelMapper modelMapper = new ModelMapper();      //TODO CORRECT
//        Sprint sprint= modelMapper.map(sprintDTO,Sprint.class);
        //TODO TMP MAP DTO->entity
        Sprint sprint = new Sprint();
        Sprint currentSprintNum=sprintDao.findFirstByProductOrderByNumberDesc(relatedTarget);
        Integer nextSprintNumber = currentSprintNum==null?1:currentSprintNum.getNumber()+1;
        sprint.setNumber(nextSprintNumber);
        sprint.setDuration(sprintDTO.getDuration());
        sprint.setProduct(relatedTarget);
        sprint.setSprintGoal(sprintDTO.getSprintGoal());
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
}
