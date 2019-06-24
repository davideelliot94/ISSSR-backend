package com.isssr.ticketing_system.entity;

import lombok.Data;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.util.List;


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


    private Boolean isActive;   //true = active, false = close, null = not actived yet

    @ManyToOne
    // Rappresenta il prodotto al quale è associato lo sprint.
    private Target product;

    @OneToMany(mappedBy = "sprint")
    private List<ScrumCeremony> scrumCeremonies;
}
