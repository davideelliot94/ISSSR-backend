package com.isssr.ticketing_system.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter

public class TargetDTO implements Serializable {

    private Long id;
    private Long scrumTeam;
    private String name;
    private String description;
    private String version;
    private String targetType;
    private String targetState;
    private String stateMachineName;

}
