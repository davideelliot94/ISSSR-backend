package com.isssr.ticketing_system.entity;

import com.isssr.ticketing_system.enumeration.BacklogItemStatus;
import com.isssr.ticketing_system.enumeration.TicketPriority;
import lombok.Data;
import lombok.ToString;
import java.sql.Date;
import java.time.LocalDate;
import javax.persistence.*;

/* Rappresenta un elemento all'interno del Product Backlog*/
@Entity
@Data
@ToString
public class BacklogItem {

    @Id
    @GeneratedValue
    private Long id;
    private String title;
    private String description;
    private Integer priority;
    private String status;
    private Integer effortEstimation;
    private LocalDate finishDate;

    @ManyToOne
    private Target product;

    @ManyToOne
    // Rappresenta lo sprint all'interno del quale si è deciso di sviluppare l'item. Può essere null
    private Sprint sprint;
}
