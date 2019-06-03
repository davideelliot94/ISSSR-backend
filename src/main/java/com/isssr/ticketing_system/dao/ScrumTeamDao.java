package com.isssr.ticketing_system.dao;

import com.isssr.ticketing_system.entity.ScrumTeam;
import com.isssr.ticketing_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScrumTeamDao extends JpaRepository<ScrumTeam, Long> {

    //torna la lista di scrumTeam in cui quel utente è Product Owner
    List<ScrumTeam> findAllByProductOwner(User ProductOwner);
}
