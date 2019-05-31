package com.isssr.ticketing_system.dao;

import com.isssr.ticketing_system.entity.BacklogItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BacklogItemDao extends JpaRepository<BacklogItem, Long> {
}
