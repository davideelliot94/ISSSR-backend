package com.isssr.ticketing_system.entity;

import com.isssr.ticketing_system.enumeration.ScrumCeremonyType;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;

/* Rappresenta un evento Scrum che può tenersi durante uno sprint*/
@Entity
@Data
@ToString
public class ScrumCeremony {
    @Id
    @GeneratedValue
    private Long id;
    private ScrumCeremonyType type;
    private String date;
    private Long duration;

    @OneToMany(mappedBy = "scrumCeremony")
    private List<ScrumCeremonyActivity> activities;

    @ManyToOne
    private Sprint sprint;

    @ManyToMany
    private List<User> participants;
}
