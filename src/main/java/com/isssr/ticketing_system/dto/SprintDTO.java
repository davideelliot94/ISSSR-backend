package com.isssr.ticketing_system.dto;

import com.fasterxml.jackson.annotation.JsonView;
import com.isssr.ticketing_system.response_entity.JsonViews;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.sql.Date;
import java.time.LocalDate;


@Data
public class SprintDTO {



    @JsonView(JsonViews.IdentifierOnly.class)
    private Long id;


    @JsonView(JsonViews.Basic.class)
    private Integer number;


    @JsonView(JsonViews.Basic.class)
    private Integer duration;// Durata dello sprint (in settimane)


    @JsonView(JsonViews.Basic.class)
    private String sprintGoal;  // Obiettivo dello sprint


    @JsonView(JsonViews.Basic.class)
    private String nameProduct;


    @JsonView(JsonViews.Basic.class)
    private Long idProduct;

    @JsonView(JsonViews.Basic.class)
    private LocalDate startDate;

    @JsonView(JsonViews.Basic.class)
    private LocalDate endDate;

    @JsonView(JsonViews.Basic.class)
    private Boolean isActive;

}
