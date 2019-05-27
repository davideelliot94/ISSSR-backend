package com.isssr.ticketing_system.entity;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "escalation")
@NoArgsConstructor
@Getter
@Setter
/*
Entity use to store the weight choosen by admin for the escalation
 */
public class Escalation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "escalation_ID")
    private Long ID;
    private Double customerPriority;
    private Double teamPriority;
    private Double time;

    public Escalation(Double customerPriority, Double teamPriority, Double time) {
        this.customerPriority = customerPriority;
        this.teamPriority = teamPriority;
        this.time = time;
    }

    public void update(@NotNull Escalation escalationUpdate){
        this.customerPriority = escalationUpdate.customerPriority;
        this.teamPriority = escalationUpdate.teamPriority;
        this.time = escalationUpdate.time;
    }
}
