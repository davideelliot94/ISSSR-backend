package com.isssr.ticketing_system.dto;

import com.fasterxml.jackson.annotation.JsonView;
import com.isssr.ticketing_system.entity.BacklogItem;
import com.isssr.ticketing_system.entity.ScrumTeam;
import com.isssr.ticketing_system.enumeration.TargetState;
import com.isssr.ticketing_system.enumeration.TargetType;
import com.isssr.ticketing_system.response_entity.JsonViews;
import lombok.Data;
import lombok.NonNull;

import javax.persistence.*;
import java.util.Collection;
import java.util.List;

@Data
public class MetadataSprintInsertDTO {
    private Long id;
    private String name;
    private String version;

    private String description;

    private TargetType targetType;
    //private String scrumTeamName;
    private Long scrumTeamid;
    private int maxSprintDurationConfigured;

    public MetadataSprintInsertDTO(Long id, String name, String version, String description, TargetType targetType,  Long scrumTeamid, int maxSprintDurationConfigured) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.description = description;
        this.targetType = targetType;
        //this.scrumTeamName = scrumTeamName;
        this.scrumTeamid = scrumTeamid;
        this.maxSprintDurationConfigured = maxSprintDurationConfigured;
    }
}
