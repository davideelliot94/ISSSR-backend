package com.isssr.ticketing_system.controller;


import com.isssr.ticketing_system.dao.RelationDao;
import com.isssr.ticketing_system.dao.RelationInstanceDao;
import com.isssr.ticketing_system.dao.TicketDao;
import com.isssr.ticketing_system.entity.Relation;
import com.isssr.ticketing_system.entity.RelationInstance;
import com.isssr.ticketing_system.entity.Ticket;
import com.isssr.ticketing_system.exception.AlreadyPresentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Service
public class RelationInstanceController {

    @Autowired
    RelationInstanceDao relationInstanceDao;

    @Autowired
    RelationDao relationDao;

    @Autowired
    TicketDao ticketDao;

    @Transactional
    public @NotNull List<Ticket> createRelationInstance(@NotNull RelationInstance relationInstance,
                                                        @NotNull String relationName,
                                                        @NotNull Long fatherId, @NotNull Long sonId) throws AlreadyPresentException {

        Relation relation =  relationDao.getOne(relationName);

        // AGGIUNTO IL 13 LUGLIO!!!!!!!!!!
        Ticket ticketFather = ticketDao.getOne(fatherId);
        Ticket ticketSon = ticketDao.getOne(sonId);

        List<Ticket> sonTickets = relationInstanceDao.findSonTicketsByRelationAndFatherTicket(relation, ticketFather);
        if(sonTickets.contains(ticketSon))
            throw new AlreadyPresentException();
       //-------!!!!!!!!!!!!!-------------
        List<Ticket> cycleList = new ArrayList<>();
        if(relationDao.findCyclicByRelation(relation)) {
            relationInstanceDao.save(relationInstance);
        }else{
            if(isAcycle(sonId, relationName, fatherId,cycleList).isEmpty())
                relationInstanceDao.save(relationInstance);
            else return cycleList;

        }
        return cycleList;
    }

    @Transactional
    public RelationInstance findRelationInstanceById(@NotNull Long id) {
        RelationInstance relation = relationInstanceDao.getOne(id);
        return relation;
    }

    @Transactional
    public List<Ticket> findTicketsByRelation(@NotNull String relationName, @NotNull Long fatherTicketId) {
        Relation relation =  relationDao.getOne(relationName);
        Ticket ticketFather = ticketDao.getOne(fatherTicketId);

        List<Ticket> sonTickets = relationInstanceDao.findSonTicketsByRelationAndFatherTicket(relation, ticketFather);

        return sonTickets;
    }

    @Transactional
    public List<RelationInstance> findRelationsByFather(@NotNull Long fatherTicketId) {
        List<RelationInstance> sonTickets = relationInstanceDao.findAllRelationInstanceByFatherTicketId(fatherTicketId);

        return sonTickets;
    }

    //NB: quando viene chiamata come fatherTicket gli passi il son e la sua lista(figli dei figli)
    @Transactional
    private List<Ticket> isAcycle(@NotNull Long fatherTicketId , @NotNull String relationName,
                                  @NotNull Long sonTicketId, List<Ticket> cycleList){
        List<Ticket> tmpList = new ArrayList<>();

        List<Ticket> sonTickets = findTicketsByRelation(relationName, fatherTicketId);
        Ticket ticketToCheck = ticketDao.getOne(sonTicketId);
        Ticket ticketRoot = ticketDao.getOne(fatherTicketId);

        if (sonTickets.isEmpty())
            return tmpList;

        if(sonTickets.contains(ticketToCheck)){
            cycleList.add(ticketRoot);
            cycleList.add(ticketToCheck);
            tmpList.addAll(cycleList);
            return tmpList;
        }else {
            for(Ticket ticket: sonTickets){
                if(!isAcycle(ticketDao.getIDByTicket(ticket), relationName, sonTicketId, cycleList).isEmpty()){
                    cycleList.add(ticketRoot);
                    tmpList.addAll(cycleList);
                    return tmpList;
                }
            }

        }

        return tmpList;

    }




}
