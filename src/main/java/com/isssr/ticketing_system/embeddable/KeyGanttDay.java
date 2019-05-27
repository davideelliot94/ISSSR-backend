package com.isssr.ticketing_system.embeddable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.isssr.ticketing_system.entity.Team;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import java.io.Serializable;

@Embeddable
//NB: annotation to declare that a class is meant to be embedded by other entities
public class KeyGanttDay implements Serializable {

    private String day;
    @OneToOne
    @JoinColumn(name = "teamName")
    @JsonIgnoreProperties
    private Team team;

    public  KeyGanttDay(){}

    public KeyGanttDay(String day, Team team) {
        this.day = day;
        this.team = team;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }


}
