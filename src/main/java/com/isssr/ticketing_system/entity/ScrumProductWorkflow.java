package com.isssr.ticketing_system.entity;

import lombok.Data;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.List;

@Entity
@Data
/* Rappresenta il workflow associato a un prodotto all'interno di processo Scrum */
public class ScrumProductWorkflow {

    @Id
    @GeneratedValue
    private Long id;
    private String name;
    @ElementCollection
    private List<String> states;

}
