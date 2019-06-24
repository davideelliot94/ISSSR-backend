package com.isssr.ticketing_system.dao;

import com.isssr.ticketing_system.entity.ScrumCeremony;
import com.isssr.ticketing_system.entity.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScrumCeremonyDao extends JpaRepository<ScrumCeremony, Long> {

    List<ScrumCeremony> findBySprint(Sprint sprint);
}
