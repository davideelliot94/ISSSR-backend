package com.isssr.ticketing_system.dao;

import com.isssr.ticketing_system.entity.Escalation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EscalationDao extends JpaRepository<Escalation, Long> {

    @Query("select e.customerPriority from Escalation e where e = :escalation")
    Double getCustomerPriorityByEscalation(@Param("escalation") Escalation escalation);

    @Query("select e.teamPriority from Escalation e where e = :escalation")
    Double getTeamPriorityByEscalation(@Param("escalation") Escalation escalation);

    @Query("select e.time from Escalation e where e = :escalation")
    Double getTimeByEscalation(@Param("escalation") Escalation escalation);
}
