package com.isssr.ticketing_system.controller;

import com.isssr.ticketing_system.dao.ScrumTeamDao;
import com.isssr.ticketing_system.dao.UserDao;
import com.isssr.ticketing_system.dto.SprintDTO;
import com.isssr.ticketing_system.entity.ScrumTeam;
import com.isssr.ticketing_system.entity.Sprint;
import com.isssr.ticketing_system.entity.Target;
import com.isssr.ticketing_system.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SprintCreateController {

    private static final int MAX_SPRINT_DURATION = 4;   //TODO MOVE IN PROPERTIES

    @Autowired
    private ScrumTeamDao scrumTeamDao;

    @Autowired
    private UserDao userDao;

    /*@Transactional
//    @LogOperation(tag = "SPRINT_CREATE", inputArgs = {"sprint"}, jsonView = JsonViews.DetailedSprint.class) //TODO ???
//    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_TEAM_MEMBER', 'ROLE_ADMIN')")       //TODO ROLE PRODUCT OWNER

    public void insertSprint(Sprint sprint) {
        //sprint check correctness
        int duration=sprint.getDuration();
        if(duration<0 || duration>MAX_SPRINT_DURATION){
            throw  new IllegalArgumentException("DURATION INVALID");
        }
        sprintDao.save(sprint);

    }*/

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
             SprintDTO sprintDTO = new SprintDTO();
             sprintDTO.setId(sprint.getId());
             sprintDTO.setDuration(sprint.getDuration());
             sprintDTO.setNumber(sprint.getNumber());
             sprintDTO.setSprintGoal(sprint.getSprintGoal());
             sprintDTO.setNameProduct(sprint.getProduct().getName());
             //TODO inserire anche il PO e SM dello Scrum Team??
             sprintDTOs.add(sprintDTO);
         }
         return sprintDTOs;

    }
}
