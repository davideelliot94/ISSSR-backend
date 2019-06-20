package com.isssr.ticketing_system.dto;

import com.fasterxml.jackson.annotation.JsonView;
import com.isssr.ticketing_system.response_entity.JsonViews;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.sql.Date;


@Data
public class SprintDTO {


    @NotNull
    @JsonView(JsonViews.IdentifierOnly.class)
    private Long id;

    @NotNull
    @JsonView(JsonViews.Basic.class)
    private Integer number;

    @NotNull
    @JsonView(JsonViews.Basic.class)
    private Integer duration;// Durata dello sprint (in settimane)


    @JsonView(JsonViews.Basic.class)
    private String sprintGoal;  // Obiettivo dello sprint

    @NotNull
    @JsonView(JsonViews.Basic.class)
    private String nameProduct;

    @NotNull
    @JsonView(JsonViews.Basic.class)
    private Long idProduct;

    @NotNull
    @JsonView(JsonViews.Basic.class)
    private Date startDate;

    @NotNull
    @JsonView(JsonViews.Basic.class)
    private Date endDate;

    @NotNull
    @JsonView(JsonViews.Basic.class)
    private boolean state;

}
