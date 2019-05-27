package com.isssr.ticketing_system.rest;


import com.isssr.ticketing_system.controller.RelationController;
import com.isssr.ticketing_system.entity.Relation;
import com.isssr.ticketing_system.logger.aspect.LogOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "relation")
@CrossOrigin
public class RelationRestService {

    @Autowired
    RelationController relationController;

    @LogOperation( inputArgs = {"Relation"}, returnObject = true)
    //NB: annotazione creata per il logging delle relazioni (integrata da altro gruppo)
    @RequestMapping(path = "{name}", method = RequestMethod.POST)
    public ResponseEntity<Relation> createRelation(@RequestBody Relation relation, @PathVariable String name) {
        Relation newRelation = relationController.createRelation(relation,name);
        return new ResponseEntity<>(newRelation, newRelation == null ? HttpStatus.FOUND : HttpStatus.CREATED);

    }

    @RequestMapping(path = "{id}", method = RequestMethod.GET)
    public ResponseEntity<Relation>  findRelation(@PathVariable String id) {
        Relation relation = relationController.findRelationById(id);
        return new ResponseEntity<>(relation, relation == null ? HttpStatus.NOT_FOUND : HttpStatus.OK);
    }


    @RequestMapping(path = "", method = RequestMethod.GET)
    public ResponseEntity<List<Relation>> findAllRelations() {
        List<Relation> relations = relationController.findAllRelations();
        return new ResponseEntity<>(relations, HttpStatus.OK);
    }

}
