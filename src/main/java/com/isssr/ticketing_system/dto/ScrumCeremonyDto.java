package com.isssr.ticketing_system.dto;

import com.isssr.ticketing_system.enumeration.ScrumCeremonyType;
import lombok.Data;

import java.util.List;

@Data
public class ScrumCeremonyDto {
    private Long id;
    private String type;
    private String date;
    private Long duration;
    private List<ScrumCeremonyActivityDto> activities;
    private Long sprintId;
    private List<UserDto> participants;
}
