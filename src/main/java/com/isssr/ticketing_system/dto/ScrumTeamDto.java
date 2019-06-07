package com.isssr.ticketing_system.dto;

import lombok.Data;

import java.util.List;

@Data
public class ScrumTeamDto {
    private Long id;
    private String name;
    private Long scrumMaster;
    private Long productOwner;
    private List<Long> teamMembers;
}
