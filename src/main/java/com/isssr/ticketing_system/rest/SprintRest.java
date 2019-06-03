package com.isssr.ticketing_system.rest;

import com.fasterxml.jackson.annotation.JsonView;
import com.isssr.ticketing_system.configuration.ConfigProperties;
import com.isssr.ticketing_system.controller.SprintCreateController;
import com.isssr.ticketing_system.controller.TargetController;
import com.isssr.ticketing_system.dto.MetadataSprintInsertDTO;
import com.isssr.ticketing_system.dto.SprintDTO;
import com.isssr.ticketing_system.entity.Sprint;
import com.isssr.ticketing_system.entity.Target;
import com.isssr.ticketing_system.exception.NotFoundEntityException;
import com.isssr.ticketing_system.response_entity.CommonResponseEntity;
import com.isssr.ticketing_system.response_entity.JsonViews;
import com.isssr.ticketing_system.response_entity.ResponseEntityBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping(path = "sprint")
@CrossOrigin("*")

public class SprintRest {
    private static final int MAX_DURATION_SPRINT = 5; //TODO configurarlo
    private SprintCreateController sprintCreateController;
    private TargetController targetController;


    @Autowired
    public SprintRest(
            SprintCreateController sprintCreateController,

            ConfigProperties configProperties //utile per predera dati dalla properties
    ) {
        this.sprintCreateController = sprintCreateController;



    }

    /*@JsonView(JsonViews.DetailedSprint.class)
    @RequestMapping(path = "", method = RequestMethod.GET)
    public ResponseEntity getMetadataInsertSprint(@RequestBody Long idProductOwner){ //TODO PRINCIPAL??
        List<Target> targets;
        try {
            targets = targetController.getTargetByProductOwnerId(idProductOwner);
        } catch (NotFoundEntityException e) {
//            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        List<MetadataSprintInsertDTO> response =new ArrayList<>();
        for (Target target: targets){
            MetadataSprintInsertDTO metadata = new MetadataSprintInsertDTO(target.getId(),target.getName(),target.getVersion(),target.getDescription(),target.getTargetType(),target.getScrumTeam().getId(), MAX_DURATION_SPRINT);

        }
        return new ResponseEntity<>(response, HttpStatus.OK);

    }*/

    /*@JsonView(JsonViews.DetailedSprint.class)
    @RequestMapping(path = "", method = RequestMethod.POST)
    public ResponseEntity insertSprint(@Valid @RequestBody Sprint sprint, @AuthenticationPrincipal Principal principal){
        sprint.setNumber(0);
        try {
            sprintCreateController.insertSprint(sprint);
            }
        catch (Exception e){
            return CommonResponseEntity.NotFoundResponseEntity("ERRORE NEL INSERIMENTO\n"+e.getMessage());
        }
        return CommonResponseEntity.CreatedResponseEntity("CREATED");
    }*/

    @JsonView(JsonViews.Basic.class)
    @RequestMapping(path = "{id}/visualize", method = RequestMethod.GET)
    public ResponseEntity getSprintProductOwner(@PathVariable Long id) {
        List<SprintDTO> sprints = sprintCreateController.getSprintsByPO(id);
        return new ResponseEntityBuilder<>(sprints).setStatus(HttpStatus.OK).build();
    }
}
