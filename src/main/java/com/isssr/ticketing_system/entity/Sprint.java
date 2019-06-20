package com.isssr.ticketing_system.entity;

import lombok.Data;
import java.sql.Date;

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

    private Integer number;
    private Integer duration;   // Durata dello sprint (in settimane)
    private String sprintGoal;  // Obiettivo dello sprint
    private Date startDate;     // Data inizio Sprint
    private Date endDate;       // Data fine Sprint
    private boolean state;      // Stato dello sprint

    @ManyToOne
    // Rappresenta il prodotto al quale è associato lo sprint.
    private Target product;
}
