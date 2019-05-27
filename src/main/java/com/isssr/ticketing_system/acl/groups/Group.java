package com.isssr.ticketing_system.acl.groups;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.isssr.ticketing_system.acl.Authority;
import com.isssr.ticketing_system.acl.Identifiable;
import com.isssr.ticketing_system.entity.SoftDelete.SoftDeletable;
import com.isssr.ticketing_system.entity.SoftDelete.SoftDeletableEntity;
import com.isssr.ticketing_system.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "group_acl")
@Data
@NoArgsConstructor
public class Group extends SoftDeletableEntity implements Identifiable {

    @Column(name = "group_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_name")
    private String name;

    @ManyToMany(fetch = FetchType.EAGER)
    //@JsonProperty(value = "sids")
    @JsonIgnore
    private List<Authority> grantedAuthorities;

    @ManyToMany
    @JoinTable(
            name = "user_type_membership",
            joinColumns = {@JoinColumn(name = "membership_group_id")},
            inverseJoinColumns = {@JoinColumn(name = "user_type_user_type_id")}
    )
    @JsonIgnore
    private List<User> members;

    public Group(String name) {

        this.name = name;
        this.members = new ArrayList<>();
        this.grantedAuthorities = new ArrayList<>();
    }

    public void addMember(User userType) {
        if (userType != null) {
            this.members.add(userType);
        }
    }

    public void setUsers(List<User> users) {
        this.members.clear();
        for (User u : users) {
            this.addMember(u);
        }
    }

    public void addAuthority(@NotNull Authority authority) {
        this.grantedAuthorities.add(authority);
    }

    public void removeAuthority(@NotNull Authority authority) {
        if (this.grantedAuthorities.contains(authority)) {
            this.grantedAuthorities.remove(authority);
        }
    }
}
