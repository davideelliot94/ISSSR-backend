package com.isssr.ticketing_system.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

/* Rappresenta un team Scrum */
@Entity
@Data
public class ScrumTeam {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private User scrumMaster;

    @ManyToOne
    private User productOwner;

    @OneToMany
    private List<User> teamMembers;

    @OneToMany(mappedBy = "scrumTeam")
    private List<Target> products;  // I prodotti sul quale lavora lo scrum team

}
