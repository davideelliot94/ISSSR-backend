package com.isssr.ticketing_system.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.GregorianCalendar;

@Entity
@Table(name = "relation_instance")
@NoArgsConstructor
@Getter
@Setter
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class RelationInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "relation_ID")
    private Long ID;
    @OneToOne
    private Ticket fatherTicket;
    @OneToOne
    private Ticket sonTicket;
    @OneToOne
    private Relation relation;


    public RelationInstance(Ticket fatherTicket, Ticket sonTicket, Relation relation) {
        this.fatherTicket = fatherTicket;
        this.sonTicket = sonTicket;
        this.relation = relation;
    }


}
