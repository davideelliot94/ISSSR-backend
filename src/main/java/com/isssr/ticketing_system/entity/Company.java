package com.isssr.ticketing_system.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.isssr.ticketing_system.entity.SoftDelete.SoftDeletableEntity;
import com.isssr.ticketing_system.response_entity.JsonViews;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;

//@Data
@NoArgsConstructor
@RequiredArgsConstructor

@Getter
@Setter

@Entity
@DynamicInsert
@DynamicUpdate
public class Company extends SoftDeletableEntity {

    @JsonView(JsonViews.IdentifierOnly.class)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonView(JsonViews.Basic.class)
    @NonNull
    private String name;
/*
    @JsonView(JsonViews.Basic.class)
    @NonNull
    private boolean enable;*/

    @JsonView(JsonViews.Basic.class)
    @NonNull
    private String domain;
/*
    //@JsonView(JsonViews.DetailedCompany.class)
    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "company_id")
    private Collection<User> teamMembers;

    public Collection<User> getTeamMembers() {
        return this.teamMembers == null ? (this.teamMembers = new ArrayList<>()) : this.teamMembers;
    }
    */
}
