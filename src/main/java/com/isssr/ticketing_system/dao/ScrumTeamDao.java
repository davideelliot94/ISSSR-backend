package com.isssr.ticketing_system.dao;

import com.isssr.ticketing_system.entity.ScrumTeam;
import com.isssr.ticketing_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public interface ScrumTeamDao extends JpaRepository<ScrumTeam, Long> {

    List<ScrumTeam> findAllByScrumMaster(User user);
    List<ScrumTeam> findAllByProductOwner(User user);
    List<ScrumTeam> findAllByTeamMembersContains(User user);
    List<ScrumTeam> findAll();

   /* @Query("select s from ScrumTeam s")
    ArrayList<ScrumTeam> getScrumTeamList();*/
}
