package com.isssr.ticketing_system.dao;

import com.isssr.ticketing_system.entity.BacklogItem;
import com.isssr.ticketing_system.entity.Sprint;
import com.isssr.ticketing_system.entity.Target;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BacklogItemDao extends JpaRepository<BacklogItem, Long> {

    List<BacklogItem> findBacklogItemByProductAndSprintIsNull(Target product);
    List<BacklogItem> findBacklogItemBySprint(Sprint sprint);
}
