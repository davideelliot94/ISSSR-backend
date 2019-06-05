package com.isssr.ticketing_system.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

/* Rappresenta un team Scrum */
@Entity
@Data
@Getter
@NoArgsConstructor
public class ScrumTeam {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @ManyToOne
    private User scrumMaster;

    @ManyToOne
    private User productOwner;

    @ManyToMany
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

    @Override
    public String toString() {
        return "ScrumTeam{" +
                "name='" + name + '\'' +
                ", scrumMaster=" + scrumMaster +
                ", productOwner=" + productOwner +
                ", teamMembers=" + teamMembers +
                '}';
    }

    public ScrumTeam(String name, User scrumMaster, User productOwner, List<User> teamMembers) {
        this.name = name;
        this.scrumMaster = scrumMaster;
        this.productOwner = productOwner;
        this.teamMembers = teamMembers;
    }

    public ScrumTeam(String name, User scrumMaster, User productOwner) {
        this.name = name;
        this.scrumMaster = scrumMaster;
        this.productOwner = productOwner;
    }
}
