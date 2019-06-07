package com.isssr.ticketing_system.dao;

import com.isssr.ticketing_system.entity.ScrumTeam;
import com.isssr.ticketing_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.ArrayList;

public interface ScrumDao extends JpaRepository<ScrumTeam, Long> {

    @Query("select s from ScrumTeam s")
    ArrayList<ScrumTeam> getScrumTeamList();

    @Query("select distinct s.teamMembers from ScrumTeam s where s.id=:scrumTeamId")
    ArrayList<User> getMembersBySTId(@Param("scrumTeamId") Long scrumTeamId);

    @Query("select distinct u from ScrumTeam s join User u on s.scrumMaster.id = u.id where s.id=:scrumTeamId")
    User getScrumMasterBySTId(@Param("scrumTeamId") Long scrumTeamId);

    @Query("select distinct u from ScrumTeam s join User u on s.productOwner.id = u.id where s.id=:scrumTeamId")
    User getProductOwnerBySTId(@Param("scrumTeamId") Long scrumTeamId);

}
