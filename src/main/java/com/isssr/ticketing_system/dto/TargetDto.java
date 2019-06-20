package com.isssr.ticketing_system.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Data
public class TargetDto implements Serializable {

    private Long id;
    private Long scrumTeamId;
    private String name;
    private String description;
    private String version;
    private String targetType;
    private String targetState;
    private String stateMachineName;
    private ScrumProductWorkflowDto scrumProductWorkflow;

}
