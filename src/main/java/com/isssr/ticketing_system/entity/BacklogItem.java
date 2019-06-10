package com.isssr.ticketing_system.entity;

import com.isssr.ticketing_system.enumeration.BacklogItemStatus;
import com.isssr.ticketing_system.enumeration.TicketPriority;
import lombok.ToString;

import javax.persistence.*;

/* Rappresenta un elemento all'interno del Product Backlog*/
@Entity
@ToString
public class BacklogItem {

    @Id
    @GeneratedValue
    private Long id;

    private String title;
    private String description;
    private TicketPriority priority;
    private BacklogItemStatus status;
    private Integer effortEstimation;

    @ManyToOne
    private Target product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id")
    private Sprint sprint;  // Rappresenta lo sprint all'interno del quale si è deciso di sviluppare l'item. Può essere null
}
