package com.isssr.ticketing_system.entity;

import Action.FSMAction;
import FSM.FSM;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.isssr.ticketing_system.acl.Identifiable;
import com.isssr.ticketing_system.enumeration.*;
import com.isssr.ticketing_system.logger.aspect.LogClass;
import com.isssr.ticketing_system.entity.SoftDelete.SoftDeletableEntity;
import com.isssr.ticketing_system.response_entity.JsonViews;
import com.isssr.ticketing_system.utils.ParseDate;
import com.isssr.ticketing_system.utils.jacksonComponents.deserializer.*;
import com.isssr.ticketing_system.utils.jacksonComponents.serializer.CreationTimestampSerializer;
import lombok.*;
import org.hibernate.annotations.*;

import javax.persistence.CascadeType;
import javax.persistence.*;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.*;

@NoArgsConstructor
@RequiredArgsConstructor

@Getter
@Setter

@Entity
@DynamicInsert
@DynamicUpdate
@FilterDef(name = "user_filter", parameters = {@ParamDef(name = "user_id", type = "long")})
@Filter(name = "user_filter", condition = "customer_id = :user_id")
@LogClass(idAttrs = {"id"})
public class Ticket extends SoftDeletableEntity implements Identifiable {
    @JsonView(JsonViews.IdentifierOnly.class)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
/*
    @JsonView(JsonViews.Basic.class)
    @NonNull
    private TicketStatus ticketStatus;
*/

    /**
     * Stato Corrente del Ticket.
     */
    @Enumerated(EnumType.STRING)
    private TicketStatus currentTicketStatus;


    @JsonView(JsonViews.DetailedTicket.class)
    @NonNull
    private TicketSource source;

    @JsonView(JsonViews.Basic.class)
    @NonNull
    @JsonSerialize(using = CreationTimestampSerializer.class)
    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss", timezone = "Europe/Rome")
    @JsonDeserialize(using = CreationTimestampDeserializer.class)
    private Instant creationTimestamp;

    @JsonView(JsonViews.Basic.class)
    @NonNull
    private TicketCategory category;

    @JsonView(JsonViews.Basic.class)
    @NonNull
    private String title;

    @JsonView(JsonViews.Basic.class)
    @NonNull
    @Column(columnDefinition = "TEXT")
    private String description;


    @JsonView(JsonViews.DetailedTicket.class)
    //@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    //@OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "ticket_id")
    //@JsonDeserialize(using = TicketAttachmentDeserializerById.class)
    //private Collection<TicketAttachment> attachments;
    private String attachments;





    @JsonView(JsonViews.Basic.class)
    @ManyToOne
    @JsonDeserialize(using = UserDeserializerById.class)
    private User assignee;

    @JsonView(JsonViews.Basic.class)
    @ManyToOne
    @NonNull
    @JsonDeserialize(using = UserDeserializerById.class)
    private User customer;

    @JsonView(JsonViews.Basic.class)
    //@NonNull
    @ManyToOne
    @JsonDeserialize(using = TargetDeserializerById.class)
    private Target target;

    @JsonView(JsonViews.Basic.class)
    @NonNull
    private TicketPriority customerPriority;

    @JsonView(JsonViews.Basic.class)
    private TicketPriority teamPriority;

    @JsonView(JsonViews.Basic.class)
    @NonNull
    private Visibility visibility;

    @JsonView(JsonViews.DetailedTicket.class)
    //@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OneToMany
    @JoinColumn(name = "ticket_id")
    @JsonDeserialize(using = TicketRelationDeserializerById.class)
    private Collection<TicketRelation> relations;

    @JsonView(JsonViews.DetailedTicket.class)
    private TicketDifficulty difficulty;

    @JsonView(JsonViews.DetailedTicket.class)
    //@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OneToMany
    @JoinColumn(name = "ticket_id")
    @JsonDeserialize(using = TicketEventDeserializerById.class)
    //@JsonIgnoreProperties
    private Collection<TicketEvent> events;

    @JsonView(JsonViews.DetailedTicket.class)
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    //@OneToMany
    @JoinColumn(name = "ticket_id")
    @JsonDeserialize(using = TicketCommentDeserializerById.class)
    private Collection<TicketComment> comments;

    private String datePendingStart;
    private String dateExecutionStart;
    private Integer durationEstimation;
    private String dateEnd;
    private Double rank;



    private long stateCounter;

    //private String presumedType;

    private String actualType;

    //private String dateStart;

    @Enumerated(EnumType.STRING)
    private TicketPriority actualPriority;

    @ElementCollection(targetClass = TAG.class)
    @Enumerated(EnumType.STRING)
    private List<TAG> tags;


    /**
     * Macchina a stati per ciascun Ticket che definisce il suo workflow.
     */
    @Lob
    @JsonIgnore
    private FSM stateMachine;

    private int TTL;

    /**
     * Informazioni sullo stato attuale del Ticket:
     *  - Azioni.
     *  - Ruoli.
     *  - Stati successivi.
     */

    private ArrayList<ArrayList<String>> stateInformation;



    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "teamName")
    @JsonIgnoreProperties
    //NB: evita che la libreria jackson generi una ricorsione infinita nella conversione di questo attributo in formato json
    private Team team;

    @OneToOne
    @JoinColumn(name = "sameTicket")
    @JsonIgnoreProperties
    //NB: tutti i ticket appertenenti ad una stessa classe della relazione "uguaglianza" fanno riferimento ad un unico
    //  ticket principale
    private Ticket sameTicket;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "dependent_tickets")
    @JsonIgnoreProperties
    //@JsonIgnore
    //NB: lista dei ticket che dipendono da questa istanza di ticket
    private Set<Ticket> dependentTickets;

    //NB: indica il numero di ticket non risolti da cui dipende questa istanza di ticket
    private Integer countDependencies;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "regressionTicketsGenerator")
    @JsonIgnoreProperties
    //NB: lista di ticket in relazione di "regressione" con questa istanza di ticket
    private Set<Ticket> regressionTicketsGenerator;
    private String teamComment;

    private boolean customerState;

    public Ticket(
            TicketStatus ticketStatus,
            TicketSource ticketSource,
            Instant creationTimestamp,
            TicketCategory ticketCategory,
            String title,
            String description,
            User assignee,
            Target target,
            TicketPriority priority,
            Visibility visibility
    ) {
        super();
        this.currentTicketStatus = ticketStatus;
        this.source = ticketSource;
        this.creationTimestamp = creationTimestamp;
        this.category = ticketCategory;
        this.title = title;
        this.description = description;
        this.assignee = assignee;
        this.target = target;
        this.customerPriority = priority;
        this.visibility = visibility;
    }

/*
    public Collection<TicketAttachment> getAttachments() {
        return this.attachments == null ? (this.attachments = new ArrayList<>()) : this.attachments;
    }
*/
    public Collection<TicketRelation> getRelations() {
        return this.relations == null ? (this.relations = new ArrayList<>()) : this.relations;
    }

    public Collection<TicketEvent> getEvents() {
        return this.events == null ? (this.events = new ArrayList<>()) : this.events;
    }

    public Collection<TicketComment> getComments() {
        return this.comments == null ? (this.comments = new ArrayList<>()) : this.comments;
    }


    public void update(@NotNull Ticket ticketUpdated)
    {
        if(ticketUpdated.currentTicketStatus !=null)
            this.currentTicketStatus = ticketUpdated.currentTicketStatus;
        if(ticketUpdated.dateEnd != null)
            this.dateEnd= ticketUpdated.dateEnd;
        if (ticketUpdated.dateExecutionStart != null)
            this.dateExecutionStart = ticketUpdated.dateExecutionStart.split(" ")[0];
        if (ticketUpdated.durationEstimation != null)
            this.durationEstimation = ticketUpdated.durationEstimation;
        if(ticketUpdated.category != null)
            this.category= ticketUpdated.category;
        if(ticketUpdated.customerPriority!=null)
            this.customerPriority= ticketUpdated.customerPriority;
        if(ticketUpdated.description != null)
            this.description=ticketUpdated.description;
        if(ticketUpdated.team != null)
            this.team = ticketUpdated.team;
        if(ticketUpdated.teamPriority!=null)
            this.teamPriority= ticketUpdated.teamPriority;
        if(ticketUpdated.title != null)
            this.title= ticketUpdated.title;
        if(ticketUpdated.sameTicket != null && !ticketUpdated.sameTicket.equals(this))
            this.sameTicket = ticketUpdated.sameTicket;
        if(ticketUpdated.teamComment != null)
            this.teamComment = ticketUpdated.teamComment;
        if(ticketUpdated.difficulty != null)
            this.difficulty = ticketUpdated.difficulty;

    }

    //NB: metodo controlla se l'aggiunta di una relazione tra questa istanza di ticket e dependentTicket crea un ciclo
    //  tra le relazione. Ritorna la lista dei ticket coinvoilti nel ciclo in caso si crei quest'ultimo, altrimenti
    //  torna una lista vuota se non si formano cicli
    public List<Ticket> isAcycle(@NotNull Ticket dependentTicket, List<Ticket> cycle){

        List<Ticket> newCycle = new ArrayList<>();
        //newCycle.addAll(cycle);
        if(this.dependentTickets.isEmpty())
            return newCycle;
        if(this.dependentTickets.contains(dependentTicket)) {
            cycle.add(dependentTicket);
            cycle.add(this);
            newCycle.addAll(cycle);
            return newCycle;
        }
        else{
            for(Ticket t: this.dependentTickets) {
                if (!t.isAcycle(dependentTicket, cycle).isEmpty()) {
                    cycle.add(this);
                    newCycle.addAll(cycle);
                    return newCycle;
                }
            }
        }

        return newCycle;

    }

    //NB: ritorna vero se dependentTicket dipennde già da questo ticket, falso altrimenti
    public boolean isAlreadyDependent(@NotNull Ticket depedentTicket){
        return this.dependentTickets.contains(depedentTicket);
    }

    public void addDependentTickets(@NotNull Ticket dependentTicket) {
        this.dependentTickets.add(dependentTicket);
    }

    public Integer addCount(){
        if(this.countDependencies==null)
            this.countDependencies=0;
        this.countDependencies++;
        return this.countDependencies;
    }

    public Integer decreaseCount(){
        if(this.countDependencies==null)
            this.countDependencies=0;
        this.countDependencies--;
        return this.countDependencies;
    }

    public Set<Ticket> decreaseDependents(){
        for(Ticket t: this.dependentTickets){
            t.decreaseCount();
        }
        return this.dependentTickets;
    }

    Integer ticketPriorityToInteger(TicketPriority priority) {
        switch (priority) {
            case LOW:
                return 0;
            case MEDIUM:
                return 3;
            case HIGH:
                return 5;
            default:
                return 0;
        }
    }

    //NB: calcolo il rank del ticket
    public Double computeRank(Double a, Double b, Double c) {
        Calendar now = Calendar.getInstance();
        GregorianCalendar datePendingStart =  ParseDate.parseGregorianCalendar(this.datePendingStart);
        Long waitingTime = (now.getTimeInMillis() -datePendingStart.getTimeInMillis());

        Double waitingTimeInHour=  waitingTime.doubleValue()/(1000*3600);
        if(this.customerPriority==null) {
            this.customerPriority = TicketPriority.LOW;
        }
        if(this.teamPriority == null){
            this.teamPriority = TicketPriority.LOW;
        }
        return  a * ticketPriorityToInteger(this.customerPriority) + b * ticketPriorityToInteger(this.teamPriority) + c * waitingTimeInHour;
    }

    public void updateRank( Double rankUpdated){
        this.rank= rankUpdated;
    }
    public String toString(){
        return this.id.toString();
    }
    public void addRegression(Ticket ticketGenerator) {
        this.regressionTicketsGenerator.add(ticketGenerator);
    }



    /**
     * Metodo usato per aggiornare l'entità con dati ricevuti dal FE.
     * @see com.isssr.ticketing_system.rest.TicketRest
     * @param ticketUpdated Un'oggetto ricevuto dal metodo REST con i valori aggiornati da un utente.
     */
    public void updateTicket(Ticket ticketUpdated) {

        if(ticketUpdated.currentTicketStatus !=null)
            this.currentTicketStatus = ticketUpdated.currentTicketStatus;
        if(ticketUpdated.dateEnd != null)
            this.dateEnd= ticketUpdated.dateEnd;
        if (ticketUpdated.dateExecutionStart != null)
            this.dateExecutionStart = ticketUpdated.dateExecutionStart.split(" ")[0];
        if (ticketUpdated.durationEstimation != null)
            this.durationEstimation = ticketUpdated.durationEstimation;
        if(ticketUpdated.category != null)
            this.category= ticketUpdated.category;
        if(ticketUpdated.customerPriority!=null)
            this.customerPriority= ticketUpdated.customerPriority;
        if(ticketUpdated.description != null)
            this.description=ticketUpdated.description;
        if(ticketUpdated.team != null)
            this.team = ticketUpdated.team;
        if(ticketUpdated.teamPriority!=null)
            this.teamPriority= ticketUpdated.teamPriority;
        if(ticketUpdated.title != null)
            this.title= ticketUpdated.title;
        if(ticketUpdated.sameTicket != null && !ticketUpdated.sameTicket.equals(this))
            this.sameTicket = ticketUpdated.sameTicket;
        if(ticketUpdated.teamComment != null)
            this.teamComment = ticketUpdated.teamComment;
        if(ticketUpdated.difficulty != null)
            this.difficulty = ticketUpdated.difficulty;

        if (ticketUpdated.creationTimestamp != null)
            this.creationTimestamp = ticketUpdated.creationTimestamp;
        /*if (ticketUpdated.presumedType != null)
            this.presumedType = ticketUpdated.presumedType;*/
        if (ticketUpdated.actualType != null)
            this.actualType = ticketUpdated.actualType;
        if (ticketUpdated.assignee != null)
            this.assignee = ticketUpdated.assignee;
        if (ticketUpdated.customer != null)
            this.customer = ticketUpdated.customer;
        if (ticketUpdated.target != null)
            this.target = ticketUpdated.target;
        if (actualPriority != null)
            this.actualPriority = ticketUpdated.actualPriority;
        if (ticketUpdated.visibility != null)
            this.visibility = ticketUpdated.visibility;
        if (ticketUpdated.comments != null)
            this.comments = ticketUpdated.comments;
        if (ticketUpdated.tags != null)
            this.tags = ticketUpdated.tags;
    }


    public void createStateMachine(String fileXMLStates) {

        try {
            this.stateMachine = new FSM(fileXMLStates, new FSMAction() {
                @Override
                public boolean action(String curState, String message, String nextState, Object args) {
                    System.out.println(curState + ":" + message + " : " + nextState);
                    return true;
                }
            });
        }
        catch (Exception e){
            System.out.println("Error\n" + e);
        }
    }

    /* ######################################################################################### */

    /* equivalencePrimary è un riferimento al ticket primario della relazione di equivalenza */
    @JsonIgnore
    @ManyToOne
    private Ticket equivalencePrimary;

    /* equivalentTickets è la lista dei ticket secondari. E' popolata solo se il ticket è primario */
    @JsonIgnore
    @OneToMany(mappedBy="equivalencePrimary")
    private List<Ticket> equivalentTickets; // punta ai ticket a cui il primario è equivalente

    /* addEquivalentTicket aggiunge un ticket alla lista dei secondari */
    public void addEquivalentTicket(Ticket equivalentTicket) {
        if (equivalentTickets == null) {
            equivalentTickets = new ArrayList<>();
        }
        equivalentTickets.add(equivalentTicket);
    }

    public void removeEquivalentTicket(Ticket equivalentTicket) {
        if (equivalentTickets != null) {
            equivalentTickets.remove(equivalentTicket);
        }
    }

    public List<Ticket> getEquivalentTickets() {
        return equivalentTickets;
    }

    public void setEquivalencePrimary(Ticket equivalencePrimary) {
        this.equivalencePrimary = equivalencePrimary;
    }

    public Ticket getEquivalencePrimary() {
        return equivalencePrimary;
    }

    // Restituisce True se il ticket è primario nella relazione di equivalenza, False altrimenti
    public boolean isEquivalencePrimary() {
        return (this.equivalencePrimary != null && this.equivalencePrimary.equals(this));
    }

    // Restituisce True se il ticket è secondario nella relazione di equivalenza, False altrimenti
    public boolean isEquivalenceSecondary(){
        return (this.equivalencePrimary != null && !this.equivalencePrimary.equals(this));
    }

    // Restituisce True se il ticket non ha nessuna relazione di equivalenza, False altrimenti
    public boolean isNotInEquivalenceRelation(){
        return this.equivalencePrimary == null;
    }
}
