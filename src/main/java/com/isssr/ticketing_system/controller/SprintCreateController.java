package com.isssr.ticketing_system.controller;

import com.isssr.ticketing_system.dao.SprintDao;
import com.isssr.ticketing_system.entity.Sprint;
import com.isssr.ticketing_system.logger.aspect.LogOperation;
import com.isssr.ticketing_system.response_entity.JsonViews;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

public class SprintCreateController {

    private static final int MAX_SPRINT_DURATION = 4;   //TODO MOVE IN PROPERTIES
    private SprintDao sprintDao;
    @Transactional
//    @LogOperation(tag = "SPRINT_CREATE", inputArgs = {"sprint"}, jsonView = JsonViews.DetailedSprint.class) //TODO ???
//    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_TEAM_MEMBER', 'ROLE_ADMIN')")       //TODO ROLE PRODUCT OWNER

    public void insertSprint(Sprint sprint) {
        //sprint check correctness
        int duration=sprint.getDuration();
        if(duration<0 || duration>MAX_SPRINT_DURATION){
            throw  new IllegalArgumentException("DURATION INVALID");
        }
        sprintDao.save(sprint);

    }
}
