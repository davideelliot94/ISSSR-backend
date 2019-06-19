package com.isssr.ticketing_system.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;


/* Rappresenta uno scrum sprint */
@Entity
@Data
public class Sprint {

    @Id
    @GeneratedValue
    private Long id;

    @GeneratedValue()
    private Integer number;
    private Integer duration;   // Durata dello sprint (in settimane)
    private String sprintGoal;  // Obiettivo dello sprint
    private boolean isActive;   //true = active, false = close, null = not actived yet

    @ManyToOne
    // Rappresenta il prodotto al quale è associato lo sprint.
    private Target product;
}
