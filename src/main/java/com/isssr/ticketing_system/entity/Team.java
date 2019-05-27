package com.isssr.ticketing_system.entity;

import com.fasterxml.jackson.annotation.JsonView;
import com.isssr.ticketing_system.acl.Identifiable;
import com.isssr.ticketing_system.enumeration.ProblemArea;
import com.isssr.ticketing_system.logger.aspect.LogClass;
import com.isssr.ticketing_system.entity.SoftDelete.SoftDeletableEntity;
import com.isssr.ticketing_system.response_entity.JsonViews;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

//@Data
@NoArgsConstructor

@Getter
@Setter

@Entity
@DynamicInsert
@DynamicUpdate
@LogClass(idAttrs = {"id"})
public class Team extends SoftDeletableEntity implements Identifiable {
    @JsonView(JsonViews.IdentifierOnly.class)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonView(JsonViews.Basic.class)
    @NonNull
    private String name;

    @JsonView(JsonViews.DetailedTeam.class)
    @NonNull
    @OneToOne
    private User teamCoordinator;

    @JsonView(JsonViews.DetailedTeam.class)
    @NonNull
    @OneToOne
    private User teamLeader;

    @JsonView(JsonViews.DetailedTeam.class)
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.DETACH)
    @JoinColumn(name = "team_id")
    private Collection<User> teamMembers;

    @Enumerated(EnumType.STRING)
    private ProblemArea problemArea;

    // teamLeader should be one of teamMembers
    public Team(String name, User teamLeader) {
        this.name = name;
        this.setTeamLeader(teamLeader);
    }

    public void setTeamLeader(User teamLeader) {
        if (this.teamLeader != null) this.getTeamMembers().remove(this.teamLeader);
        this.getTeamMembers().add(teamLeader);
        this.teamLeader = teamLeader;
    }

    public Collection<User> getTeamMembers() {
        return this.teamMembers == null ? (this.teamMembers = new ArrayList<>()) : this.teamMembers;
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




    /**
     * Metodo usato per aggiornare l'entità con dati ricevuti dal FE.
     * @see com.isssr.ticketing_system.rest.TeamRest
     * @param teamUpdated Un'oggetto ricevuto dal metodo REST con i valori aggiornati da un utente.
     */
    public void updateTeam(@NotNull Team teamUpdated) {
       /* if (teamUpdated.teamCoordinator != null)
            this.teamCoordinator = teamUpdated.teamCoordinator;*/
        if (teamUpdated.teamLeader != null)
            this.teamLeader = teamUpdated.teamLeader;
        if (teamUpdated.teamMembers != null)
            this.teamMembers = teamUpdated.teamMembers;
        if (teamUpdated.problemArea != null)
            this.problemArea = teamUpdated.problemArea;
    }

}
