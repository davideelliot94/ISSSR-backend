package com.isssr.ticketing_system.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import com.isssr.ticketing_system.acl.Identifiable;
import com.isssr.ticketing_system.enumeration.TargetState;
import com.isssr.ticketing_system.enumeration.TargetType;
import com.isssr.ticketing_system.logger.aspect.LogClass;
import com.isssr.ticketing_system.entity.SoftDelete.SoftDeletableEntity;
import com.isssr.ticketing_system.response_entity.JsonViews;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;


/**
 * Il Target e' l'entita' per la quale il sistema di Ticketing offre assistenza.
 * Puo' essere un Prodotto o un Servizio.
 *
 */
@NoArgsConstructor
@RequiredArgsConstructor
@Entity
@Getter
@Setter
@DynamicInsert
@DynamicUpdate
@LogClass(idAttrs = {"id"})
public class Target extends SoftDeletableEntity implements Identifiable {
    @JsonView(JsonViews.IdentifierOnly.class)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonView(JsonViews.Basic.class)
    @NonNull
    private String name;

    @JsonView(JsonViews.Basic.class)
    @NonNull
    private String version;

    @JsonView(JsonViews.Basic.class)
    private String description;

    @JsonView(JsonViews.DetailedTarget.class)
    @Enumerated(EnumType.STRING)
    private TargetState targetState;

    @JsonView(JsonViews.DetailedTarget.class)
    @Enumerated(EnumType.STRING)
    private TargetType targetType;

    /**
     *  NOME della Macchina a stati associata allo specifico Target.
     */
    @JsonView(JsonViews.DetailedTarget.class)
    private String stateMachineName;

    @JsonView(JsonViews.DetailedTarget.class)
    @ElementCollection
    private Collection<String> categories;

    @JsonIgnore // Per integrarlo con le entity presenti, che vengono PROCESSATE DIRETTAMENTE DALLA BOUNDARY!!!
    @OneToMany(mappedBy = "product")
    private List<BacklogItem> backlogItems;

    @JsonIgnore // Per integrarlo con le entity presenti, che vengono PROCESSATE DIRETTAMENTE DALLA BOUNDARY!!!
    @OneToMany(mappedBy = "product")
    private List<Sprint> sprints;

    @JsonIgnore // Per integrarlo con le entity presenti, che vengono PROCESSATE DIRETTAMENTE DALLA BOUNDARY!!!
    @ManyToOne
    private ScrumTeam scrumTeam;

    @JsonIgnore // Per integrarlo con le entity presenti, che vengono PROCESSATE DIRETTAMENTE DALLA BOUNDARY!!!
    @ManyToOne
    private ScrumProductWorkflow scrumProductWorkflow;

    /**
     * Metodo usato per aggiornare l'entità con dati ricevuti dal FE.
     * @see com.isssr.ticketing_system.rest.TargetRest
     * @param targetUpdated  Un'oggetto ricevuto dal metodo REST con i valori aggiornati da un utente.
     */
    public void updateTarget(@NotNull Target targetUpdated) {
        if (targetUpdated.name != null)
            this.name = targetUpdated.name;
        if(targetUpdated.version != null)
            this.version = targetUpdated.version;
        if(targetUpdated.description != null)
            this.description = targetUpdated.description;
        if(targetUpdated.targetType != null)
            this.targetType = targetUpdated.targetType;
        if(targetUpdated.targetState!= null)
            this.targetState = targetUpdated.targetState;
        if(targetUpdated.categories != null)
            this.categories = targetUpdated.categories;
        if(targetUpdated.stateMachineName != null)
            this.stateMachineName = targetUpdated.stateMachineName;
    }

}
