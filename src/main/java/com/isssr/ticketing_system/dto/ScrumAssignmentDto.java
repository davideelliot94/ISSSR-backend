package com.isssr.ticketing_system.dto;

import lombok.Data;

// Rappresenta un'associazione tra uno Scrum Team e il prodotto su cui lavora (con il workflow corrispondente)
@Data
public class ScrumAssignmentDto {
    private String product;
    private String scrumTeam;
    private String scrumProductWorkflow;
}
