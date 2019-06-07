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
public class TargetDto {
    private Long id;
    private String name;
    private String version;
    private String description;
    private TargetType targetType;
//    private String scrumTeamName;
      private Long scrumTeamId;

//    public TargetDto(Long id, String name, String version, String description, TargetType targetType, Long scrumTeamId) {
//        this.id = id;
//        this.name = name;
//        this.version = version;
//        this.description = description;
//        this.targetType = targetType;
//        this.scrumTeamId = scrumTeamId;
//    }
}
