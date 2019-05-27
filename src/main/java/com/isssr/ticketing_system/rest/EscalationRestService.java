package com.isssr.ticketing_system.rest;


import com.isssr.ticketing_system.controller.EscalationController;
import com.isssr.ticketing_system.entity.Escalation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "escalation")
@CrossOrigin
public class EscalationRestService {

    @Autowired
    EscalationController escalationController;

    //NB: crea un'escalation, ovvero fa partire un nuovo thread con una nuova relazione (con i parametri definiti dall'admin)
    // per il calcolo del rank e killa il precedente
    @RequestMapping(path = "", method = RequestMethod.POST)
    public ResponseEntity<Escalation> createEscalation(@RequestBody Escalation escalation) {
        Escalation newEscalation = escalationController.createEscalation(escalation);
        return new ResponseEntity<>(newEscalation, HttpStatus.CREATED);
    }
}
