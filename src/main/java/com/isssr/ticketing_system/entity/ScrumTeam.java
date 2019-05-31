package com.isssr.ticketing_system.entity;

import lombok.Getter;
import lombok.NonNull;

import javax.persistence.*;
import java.util.List;

/* Rappresenta un team Scrum */
@Entity
@Getter
public class ScrumTeam {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @ManyToOne
    private User scrumMaster;

    @ManyToOne
    private User productOwner;

    @OneToMany
    private List<User> teamMembers;

    @OneToMany(mappedBy = "scrumTeam")
    private List<Target> products;  // I prodotti sul quale lavora lo scrum team

    public void setScrumMaster(User scrumMaster) {
        this.scrumMaster = scrumMaster;
    }

    public void setProductOwner(User productOwner) {
        this.productOwner = productOwner;
    }

    public void addUsers(List<User> users) {
        this.teamMembers.clear();
        for (User ut : users) {
            this.addMember(ut);
        }
    }

    public void addMember(User member) {
        this.teamMembers.add(member);
    }
}
