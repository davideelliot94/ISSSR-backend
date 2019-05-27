package com.isssr.ticketing_system.dao;

import com.isssr.ticketing_system.embeddable.KeyGanttDay;
import com.isssr.ticketing_system.entity.GanttDay;
import com.isssr.ticketing_system.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface GanttDayDao extends JpaRepository<GanttDay,KeyGanttDay> {

    @Query("SELECT g.availability FROM GanttDay g where g.keyGanttDay = :keyGanttDay")
    Double getAvailabilityByDayAndTeam(@Param("keyGanttDay") KeyGanttDay keyGanttDay);


    GanttDay findByKeyGanttDay(KeyGanttDay keyGanttDay);

    @Query("SELECT g.tickets FROM GanttDay g where g.keyGanttDay = :keyGanttDay")
    List<Ticket> getTicketsByKey(@Param("keyGanttDay") KeyGanttDay keyGanttDay);

    @Query("SELECT g.tickets FROM GanttDay g where g.keyGanttDay = :keyGanttDay")
    Set<Ticket> getTicketsSetByKey(@Param("keyGanttDay") KeyGanttDay keyGanttDay);
}
