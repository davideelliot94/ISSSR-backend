package com.isssr.ticketing_system.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.isssr.ticketing_system.enumeration.UserRole;
import com.isssr.ticketing_system.logger.aspect.LogClass;
import com.isssr.ticketing_system.entity.SoftDelete.SoftDeletableEntity;
import com.isssr.ticketing_system.response_entity.JsonViews;
import com.isssr.ticketing_system.utils.jacksonComponents.deserializer.CompanyDeserializerById;
import com.isssr.ticketing_system.utils.jacksonComponents.views.Views;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.Collection;
import java.util.List;

//@Data
@NoArgsConstructor
@RequiredArgsConstructor

@Getter
@Setter

@Entity
@DynamicInsert
@DynamicUpdate
@LogClass(idAttrs = {"id"})
@Table(name = "users")
public class User extends SoftDeletableEntity {
    @JsonView(JsonViews.IdentifierOnly.class)
    @Id
    @GeneratedValue
    private Long id;

    @JsonView(JsonViews.Basic.class)
    @NonNull
    private String firstName;

    @JsonView(JsonViews.Basic.class)
    @NonNull
    private String lastName;

    @JsonView(JsonViews.Basic.class)
    @NonNull
    private String email;

    @JsonView(JsonViews.Basic.class)
    @NonNull
    private String username;

    @NonNull
    @JsonView(Views.Secrets.class)
    private String password;

    @JsonView(JsonViews.DetailedUser.class)
    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    @JsonDeserialize(using = CompanyDeserializerById.class)
    //@JsonSerialize(using = CompanySerializerById.class)
    private Company company;

    @JsonView(JsonViews.Basic.class)
    @NonNull
    @JoinTable(name = "user_role")
    //@JsonSerialize(using = RoleSerializerById.class)
    private UserRole role;
    
    //@JsonView(JsonViews.DetailedUser.class)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    @JsonIgnore
    private Team team;

    @ManyToMany
    @JsonIgnore // Per integrarlo con le entity presenti, che vengono PROCESSATE DIRETTAMENTE DALLA BOUNDARY!!!
    private List<ScrumTeam> scrumTeams;

/*
    //@JsonView(JsonViews.DetailedUser.class)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_leader_id")
    @JsonIgnore
    private Team teamLeader;
    */
/*
    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "customer_id")
    private Collection<Ticket> tickets;
    */
}

