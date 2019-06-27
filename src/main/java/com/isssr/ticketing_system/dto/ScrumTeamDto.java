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


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getScrumMaster() {
        return scrumMaster;
    }

    public void setScrumMaster(Long scrumMaster) {
        this.scrumMaster = scrumMaster;
    }

    public Long getProductOwner() {
        return productOwner;
    }

    public void setProductOwner(Long productOwner) {
        this.productOwner = productOwner;
    }

    public List<Long> getTeamMembers() {
        return teamMembers;
    }

    public void setTeamMembers(List<Long> teamMembers) {
        this.teamMembers = teamMembers;
    }
}
