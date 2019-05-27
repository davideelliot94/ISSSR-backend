package com.isssr.ticketing_system.dao;

import com.isssr.ticketing_system.entity.Relation;
import com.isssr.ticketing_system.entity.RelationInstance;
import com.isssr.ticketing_system.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RelationInstanceDao extends JpaRepository<RelationInstance,Long> {

    @Query ("select r.sonTicket from RelationInstance r where r.relation = :relation and r.fatherTicket = :fatherTicket ")
    List<Ticket> findSonTicketsByRelationAndFatherTicket(@Param("relation") Relation relation, @Param("fatherTicket") Ticket fatherTicket);

    List<RelationInstance> findAllRelationInstanceByFatherTicketId(Long fatherTicketId);




}