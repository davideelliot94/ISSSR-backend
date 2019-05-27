package com.isssr.ticketing_system.thread;

import com.isssr.ticketing_system.dao.TicketDao;
import com.isssr.ticketing_system.entity.Ticket;
import com.isssr.ticketing_system.enumeration.TicketStatus;
import com.isssr.ticketing_system.utils.State;

import java.util.List;


/*
NB: thread che:
 -attiva la funzione computeRank su tutti i ticket in pending
 -aggiorna il campo rank di ogni ticket in pending
 -e salva ogni ticket nel database

versione usata senza l'annotazione @Scheduled
 */
public class ThreadEscalation implements Runnable{


    private Double customerPriority;
    private Double teamPriority;
    private Double time;
    private TicketDao ticketDao;



    public ThreadEscalation(Double customerPriority, Double teamPriority, Double time, TicketDao ticketDao) {
        this.customerPriority = customerPriority;
        this.teamPriority = teamPriority;
        this.time = time;
        this.ticketDao=ticketDao;

    }

    @Override
    public void run() {


        List<Ticket> pendingTickets = ticketDao.findDistinctByCurrentTicketStatus(TicketStatus.PENDING);

        for(Ticket ticket: pendingTickets){
                Double rank = ticket.computeRank(customerPriority,teamPriority,time);
                ticket.updateRank(rank);
                ticketDao.save(ticket);

        }

    }
}
