package com.isssr.ticketing_system.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;


/* Rappresenta uno scrum sprint */
@Entity
public class Sprint {

    @Id
    @GeneratedValue
    private Long id;

    private Integer number;
    private Integer duration;   // Durata dello sprint (in settimane)
    private String sprintGoal;  // Obiettivo dello sprint
}
