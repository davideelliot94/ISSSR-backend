package com.isssr.ticketing_system.dao;

import com.isssr.ticketing_system.entity.BacklogItem;
import com.isssr.ticketing_system.entity.Sprint;
import com.isssr.ticketing_system.entity.Target;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BacklogItemDao extends JpaRepository<BacklogItem, Long> {

    List<BacklogItem> findBacklogItemByProductAndSprintIsNull(Target product);
    List<BacklogItem> findBacklogItemBySprint(Sprint sprint);

    @Query("select MAX (b.priority) from BacklogItem b where b.product.id=:productId")
    Integer getMaxPriority(@Param("productId") Long productId);

    @Query("select sum(i.effortEstimation) - (select coalesce(sum(i2.effortEstimation), 0) from BacklogItem i2 where i2.sprint.id=:sprintId and i2.finishDate<=:date) from BacklogItem i where i.sprint.id=:sprintId")
    Integer getFinishedBacklogItem(@Param("sprintId") Long sprintId, @Param("date") LocalDate date);

    @Query("select sum(b.effortEstimation) from BacklogItem b where b.sprint.id = :sprintId")
    Integer countInitialEffort(@Param("sprintId") Long sprintId);


}
