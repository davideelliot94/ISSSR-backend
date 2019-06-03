package com.isssr.ticketing_system.rest;

import com.fasterxml.jackson.annotation.JsonView;
import com.isssr.ticketing_system.configuration.ConfigProperties;
import com.isssr.ticketing_system.controller.SprintCreateController;
import com.isssr.ticketing_system.controller.TargetController;

import com.isssr.ticketing_system.dto.SprintDTO;
import com.isssr.ticketing_system.dto.TargetDTO;
import com.isssr.ticketing_system.entity.Sprint;
import com.isssr.ticketing_system.entity.Target;
import com.isssr.ticketing_system.exception.NotFoundEntityException;
import com.isssr.ticketing_system.response_entity.CommonResponseEntity;
import com.isssr.ticketing_system.response_entity.JsonViews;
import com.isssr.ticketing_system.response_entity.ResponseEntityBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.hibernate.query.criteria.internal.expression.function.AggregationFunction;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

//login
//curl 'http://localhost:8200/ticketingsystem/public/login/'  -H 'Content-Type: application/json;charset=utf-8'  --data '{"username":"admin","password":"password"}' -v
//get metadata for sprint insert
//curl 'http://localhost:8200/ticketingsystem/sprint/create/1'  -v -H 'Authorization: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImF1ZGllbmNlIjoid2ViIiwicm9sZXMiOlsiUk9MRV9BRE1JTiIsIlJPTEVfVEVBTV9DT09SRElOQVRPUiIsIlJPTEVfR1JPVVBfQ09PUkRJTkFUT1IiLCJST0xFX1NPRlRXQVJFX1BST0RVQ1RfQ09PUkRJTkFUT1IiXSwiaXNFbmFibGVkIjp0cnVlLCJleHAiOjE1NTk1NTgyNzUsImlhdCI6MTU1OTU1MTA3NTQ2NX0.Vj5hXgDO2IEgUijQ3fm6gIzWAzhU8wm36lHA30Qpy38'
//curl 'http://localhost:8200/ticketingsystem/sprint/create/1'  -v -H 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImF1ZGllbmNlIjoid2ViIiwicm9sZXMiOlsiUk9MRV9BRE1JTiIsIlJPTEVfVEVBTV9DT09SRElOQVRPUiIsIlJPTEVfR1JPVVBfQ09PUkRJTkFUT1IiLCJST0xFX1NPRlRXQVJFX1BST0RVQ1RfQ09PUkRJTkFUT1IiXSwiaXNFbmFibGVkIjp0cnVlLCJleHAiOjE1NTk1NzY2NzIsImlhdCI6MTU1OTU2OTQ3MjM3Nn0.3UFLx_OL7eaG-JH4sESHdve7tATwElMqi9HdgRFa5wE'
//post sprint insert
//curl 'http://localhost:8200/ticketingsystem/sprint/create/'  -H 'Content-Type: applica"number":-1,"duration":"2","sprintGoal":"null"}' -v  -H 'Authorization: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImF1ZGllbmNlIjoid2ViIiwicm9sZXMiOlsiUk9MRV9BRE1JTiIsIlJPTEVfVEVBTV9DT09SRElOQVRPUiIsIlJPTEVfR1JPVVBfQ09PUkRJTkFUT1IiLCJST0xFX1NPRlRXQVJFX1BST0RVQ1RfQ09PUkRJTkFUT1IiXSwiaXNFbmFibGVkIjp0cnVlLCJleHAiOjE1NTk1NTgyNzUsImlhdCI6MTU1OTU1MTA3NTQ2NX0.Vj5hXgDO2IEgUijQ3fm6gIzWAzhU8wm36lHA30Qpy38'

@Validated
@RestController
@RequestMapping("sprint")
@CrossOrigin("*")
public class SprintRest {
    private static final String MAX_DURATION_SPRINT = "5"; //TODO configurarlo in properties
    @Autowired
    private SprintCreateController sprintCreateController;
    @Autowired
    private TargetController targetController;


    @Autowired
    public SprintRest(
            SprintCreateController sprintCreateController,

            ConfigProperties configProperties //utile per predera dati dalla properties
    ) {
        this.sprintCreateController = sprintCreateController;


    }


    @RequestMapping(path = "create/{idProductOwner}", method = RequestMethod.GET)
    public ResponseEntity getMetadataInsertSprint(@PathVariable Long idProductOwner) { //TODO PRINCIPAL??
        List<TargetDTO> targets;
        try {
            targets = targetController.getTargetByProductOwnerId(idProductOwner);
        } catch (NotFoundEntityException e) {
//            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.OK);
        }
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("MAX_ALLOWED_SPRINT_DURATION", MAX_DURATION_SPRINT); //set max sprint duration costraint in the header
        return new ResponseEntity<>(targets, headers, HttpStatus.OK);

    }

    //WINDOWS POST CURL
    //curl "http://localhost:8200/ticketingsystem/sprint/create"  -H "Content-Type: application/json;charset=utf-8" -H "Authorization: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImF1ZGllbmNlIjoid2ViIiwicm9sZXMiOlsiUk9MRV9BRE1JTiIsIlJPTEVfVEVBTV9DT09SRElOQVRPUiIsIlJPTEVfR1JPVVBfQ09PUkRJTkFUT1IiLCJST0xFX1NPRlRXQVJFX1BST0RVQ1RfQ09PUkRJTkFUT1IiXSwiaXNFbmFibGVkIjp0cnVlLCJleHAiOjE1NTk1ODY5MDEsImlhdCI6MTU1OTU3OTcwMTY2Mn0.duOTGomAj0LObj9y5U3AZ9W-aQG3OSRPQRCGOuByn-I"  --data "{""duration"":1}"

//    @JsonView(JsonViews.Basic.class)
    @RequestMapping(path = "/create", method = RequestMethod.POST)
    public ResponseEntity insertSprint(@RequestBody SprintDTO sprintDTO, @AuthenticationPrincipal Principal principal) {    //TODO Principal binding ?
        sprintDTO.setNumber(0);
        System.err.println(96);
        try {
            sprintCreateController.insertSprint(sprintDTO);
        } catch (Exception e) {
            return CommonResponseEntity.NotFoundResponseEntity("ERRORE NEL INSERIMENTO\n" + e.getMessage());
        }
        return CommonResponseEntity.CreatedResponseEntity("CREATED", "Sprint");//TODO ticket?

    }


    @JsonView(JsonViews.Basic.class)
    @RequestMapping(path = "{id}/visualize", method = RequestMethod.GET)
    public ResponseEntity getSprintProductOwner(@PathVariable Long id) {
        List<SprintDTO> sprints = sprintCreateController.getSprintsByPO(id);
        return new ResponseEntityBuilder<>(sprints).setStatus(HttpStatus.OK).build();
    }
}