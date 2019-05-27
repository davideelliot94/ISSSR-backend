package com.isssr.ticketing_system.dao;

import com.isssr.ticketing_system.entity.TicketRelation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRelationRepository extends CrudRepository<TicketRelation, Long> {
}
