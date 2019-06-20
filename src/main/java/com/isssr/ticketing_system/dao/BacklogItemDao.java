package com.isssr.ticketing_system.dao;

import com.isssr.ticketing_system.embeddable.KeyGanttDay;
import com.isssr.ticketing_system.entity.BacklogItem;
import com.isssr.ticketing_system.entity.Sprint;
import com.isssr.ticketing_system.entity.Target;
import com.isssr.ticketing_system.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BacklogItemDao extends JpaRepository<BacklogItem, Long> {

    List<BacklogItem> findBacklogItemByProductAndSprintIsNull(Target product);
    List<BacklogItem> findBacklogItemBySprint(Sprint sprint);

    @Query("select i from BacklogItem i where i.finishDate is not null and i.sprint =: sprintId")
    List<BacklogItem> getFinishedBacklogItem(@Param("sprintId") Long sprintId);

}
