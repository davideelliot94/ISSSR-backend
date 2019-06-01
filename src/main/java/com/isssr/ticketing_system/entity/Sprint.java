package com.isssr.ticketing_system.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;


/* Rappresenta uno scrum sprint */
@Entity
@Getter
public class Sprint {

    @Id
    @GeneratedValue
    private Long id;
    @Setter
    private Integer number;
    private Integer duration;   // Durata dello sprint (in settimane)
    private String sprintGoal;  // Obiettivo dello sprint
}
