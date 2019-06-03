package com.isssr.ticketing_system.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.isssr.ticketing_system.response_entity.JsonViews;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.repository.NoRepositoryBean;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Collection;


/* Rappresenta uno scrum sprint */
@Entity
@Data
public class Sprint {

    @Id
    @GeneratedValue
    private Long id;

    @NotNull
    @Setter
    private Integer number;

    @NotNull
    private Integer duration;// Durata dello sprint (in settimane)


    private String sprintGoal;  // Obiettivo dello sprint

    @NotNull
    @ManyToOne
    private Target product;


}
