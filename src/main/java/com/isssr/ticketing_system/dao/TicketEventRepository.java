package com.isssr.ticketing_system.dao;

import com.isssr.ticketing_system.entity.TicketEvent;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketEventRepository extends CrudRepository<TicketEvent, Long> {
}
