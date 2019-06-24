package com.isssr.ticketing_system.entity;

import lombok.Data;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/*Rappresenta un'attività svoltasi durante una ScrumCeremonyActivity. L'attività ha un nome
* e un commento*/

@Entity
@Data
@ToString
public class ScrumCeremonyActivity {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String comment;

    @ManyToOne
    private ScrumCeremony scrumCeremony;
}
