package com.isssr.ticketing_system.dto;

import com.isssr.ticketing_system.enumeration.TargetType;
import lombok.Data;

@Data
public class TargetDto {
    private Long id;
    private String name;
    private String version;
    private String description;
    private TargetType targetType;
    private Long scrumTeamId;
}
