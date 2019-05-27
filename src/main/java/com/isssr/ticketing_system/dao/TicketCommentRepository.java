package com.isssr.ticketing_system.dao;

import com.isssr.ticketing_system.entity.TicketComment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketCommentRepository extends CrudRepository<TicketComment, Long> {
}
