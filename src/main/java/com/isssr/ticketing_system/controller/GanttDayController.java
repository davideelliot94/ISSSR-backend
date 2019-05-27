package com.isssr.ticketing_system.controller;

import com.isssr.ticketing_system.dao.GanttDayDao;
import com.isssr.ticketing_system.dao.TeamDao;
import com.isssr.ticketing_system.dao.TicketDao;
import com.isssr.ticketing_system.embeddable.KeyGanttDay;
import com.isssr.ticketing_system.entity.GanttDay;
import com.isssr.ticketing_system.entity.Team;
import com.isssr.ticketing_system.entity.Ticket;
import com.isssr.ticketing_system.entity.User;
import com.isssr.ticketing_system.exception.DependeciesFoundException;
import com.isssr.ticketing_system.exception.EntityNotFoundException;
import com.isssr.ticketing_system.utils.ParseDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.*;

@Service
public class GanttDayController {
    @Autowired
    GanttDayDao ganttDayDao;
    @Autowired
    TeamDao teamDao;
    @Autowired
    TicketDao ticketDao;


    @Transactional
    public GanttDay createGanttDay(GanttDay ganttDay){
        GanttDay ganttDayNew = ganttDayDao.save(ganttDay);
        return ganttDayNew;
    }


    @Transactional
    public @NotNull List<GanttDay> createGanttInstance(@NotNull Ticket ticket, @NotNull String teamName,
                                                       @NotNull String firstDay, @NotNull Integer duration,
                                                       @NotNull Long ticketId) throws DependeciesFoundException, EntityNotFoundException {

        List<GanttDay> ganttDays = new ArrayList<>();

        Optional<Team> team = teamDao.findByName(teamName);
        if (!team.isPresent()) {
            throw new EntityNotFoundException("Team not found");
        }
        Ticket ticketToUpdate = ticketDao.getOne(ticketId);

        GregorianCalendar first = ParseDate.parseGregorianCalendar(firstDay);

        //check dependencies
        Set<Ticket> tickets = new HashSet<>();
        tickets.add(ticketToUpdate);
        List<Ticket> fatherTickets = ticketDao.findDistinctByDependentTicketsContains(tickets);
        for (Ticket father : fatherTickets) {
            Integer durationFather = ticketDao.findDurationByTicket(father);
            if (durationFather == null) {
                throw new DependeciesFoundException();
            }
            GregorianCalendar dateExecStartFather = ParseDate.parseGregorianCalendar(ticketDao.findDateExecutionByTicket(father));
                        dateExecStartFather.add(Calendar.DAY_OF_MONTH,durationFather);
            if (dateExecStartFather.compareTo(first) > 0) {
                throw new DependeciesFoundException();
            }
        }

        //check availability for all day
        for(int i = 0; i<duration; i++) {
            String currentDay = ParseDate.gregorianCalendarToString(first);
            KeyGanttDay keyGanttDay = new KeyGanttDay(currentDay,team.get());
            if (!ganttDayDao.existsById(keyGanttDay)) {
                continue;
            }
            Double currentAvail = ganttDayDao.getAvailabilityByDayAndTeam(keyGanttDay);
            if (currentAvail >= 1) {
                ganttDays.add(ganttDayDao.getOne(keyGanttDay));
            }
            first.add(Calendar.DAY_OF_MONTH, 1);
        }

        if (!ganttDays.isEmpty()) {
            return ganttDays;
        }

        first = ParseDate.parseGregorianCalendar(firstDay);

        for(int i = 0; i<duration; i++) {
            String currentDay = ParseDate.gregorianCalendarToString(first);
            updateGanttDay(currentDay, ticketToUpdate, team.get());
            first.add(Calendar.DAY_OF_MONTH, 1);
        }

        ticket.setTeam(team.get());
        ticketToUpdate.update(ticket);
        ticketDao.save(ticketToUpdate);

        return ganttDays;
    }

    @Transactional
    private Double computeAvailability(Team team, Integer ticketsSize) {
        List<User> teamMember = teamDao.findTeamMembersByTeam(team);
        return (double) ticketsSize /((double)teamMember.size() + 2);

    }

    @Transactional
    public void updateGanttDay( String day, Ticket ticketToUpdate , Team team) {
       KeyGanttDay keyGanttDay = new KeyGanttDay(day, team);
        Set<Ticket> ticketSet = new HashSet<>();
        GanttDay ganttDayToUpdate;
        if (!ganttDayDao.existsById(keyGanttDay)) {
            GanttDay ganttDayToSave = new GanttDay(keyGanttDay,0.0,ticketSet);
            ganttDayToUpdate = ganttDayDao.save(ganttDayToSave);
        } else{
            ganttDayToUpdate = ganttDayDao.findByKeyGanttDay(keyGanttDay);
            ticketSet = ganttDayDao.getTicketsSetByKey(keyGanttDay);
        }
        ticketSet.add(ticketToUpdate);
        Double newAvail = computeAvailability(team, ticketSet.size());
        GanttDay ganttDayUpdated = new GanttDay(keyGanttDay,newAvail,ticketSet);
        ganttDayToUpdate.update(ganttDayUpdated);
        ganttDayDao.save(ganttDayToUpdate);
    }
}