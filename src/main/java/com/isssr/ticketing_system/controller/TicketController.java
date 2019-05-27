package com.isssr.ticketing_system.controller;

import FSM.FSM;
import com.isssr.ticketing_system.acl.defaultpermission.TargetDefaultPermission;
import com.isssr.ticketing_system.acl.defaultpermission.TicketDefaultPermission;
import com.isssr.ticketing_system.dao.GanttDayDao;
import com.isssr.ticketing_system.dao.TeamDao;
import com.isssr.ticketing_system.embeddable.KeyGanttDay;
import com.isssr.ticketing_system.entity.*;
import com.isssr.ticketing_system.enumeration.TicketDifficulty;
import com.isssr.ticketing_system.enumeration.TicketPriority;
import com.isssr.ticketing_system.enumeration.TicketStatus;
import com.isssr.ticketing_system.enumeration.Visibility;
import com.isssr.ticketing_system.exception.DependeciesFoundException;
import com.isssr.ticketing_system.exception.EntityNotFoundException;
import com.isssr.ticketing_system.exception.NotFoundEntityException;
import com.isssr.ticketing_system.logger.aspect.LogOperation;
import com.isssr.ticketing_system.entity.SoftDelete.SoftDelete;
import com.isssr.ticketing_system.entity.SoftDelete.SoftDeleteKind;
import com.isssr.ticketing_system.dao.TicketDao;
import com.isssr.ticketing_system.response_entity.JsonViews;
import com.isssr.ticketing_system.utils.ParseDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;


@Service
@SoftDelete(SoftDeleteKind.NOT_DELETED)
public class TicketController {

    private UserController userController;
    private TeamDao teamDao;
    private TicketDefaultPermission defaultPermissionTable;
    private TicketDao ticketDao;
    private TeamController teamController;
    private GanttDayDao ganttDayDao;
    private GanttDayController ganttDayController;

    @Autowired
    public TicketController(
            UserController userController,
            TicketDao ticketDao,
            TeamDao teamDao,
            TicketDefaultPermission defaultPermissionTable,
            TeamController teamController,
            GanttDayDao ganttDayDao,
            GanttDayController ganttDayController
    ) {
        this.userController = userController;
        this.teamController = teamController;
        this.ticketDao = ticketDao;
        this.teamDao = teamDao;
        this.defaultPermissionTable = defaultPermissionTable;
        this.ganttDayDao = ganttDayDao;
        this.ganttDayController = ganttDayController;
    }

    @Transactional
    @LogOperation(tag = "TICKET_CREATE", inputArgs = {"ticket"}, jsonView = JsonViews.DetailedTicket.class)
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_TEAM_MEMBER', 'ROLE_ADMIN')")
    public Ticket insertTicket(Ticket ticket) {

        String stateMachineFileName = ticket.getTarget().getStateMachineName();
        //String relativePath = "./src/main/resources/status_machine/xml_files/";
        String relativePath = "./src/main/resources/state_machine/xml_files/";
        ticket.createStateMachine( relativePath + stateMachineFileName + ".xml");

        TicketStatus currentTicketStatus = TicketStatus.getEnum(ticket.getStateMachine().getCurrentState());
        //if(currentTicketStatus ==null)
          //  throw new NotFoundEntityException();

        ticket.setCurrentTicketStatus(currentTicketStatus);
        ticket.setTTL(currentTicketStatus.getTTL());
        ticket.setStateCounter(System.currentTimeMillis());
        FSM stateMachine = ticket.getStateMachine();
        ticket.setStateInformation(stateMachine.getStateInformation(currentTicketStatus.toString()));
        ticket.setAssignee(userController.getTeamCoordinator());
        ticket.setCustomerState(false);
        ticket.setVisibility(Visibility.PUBLIC);

        Ticket newTicket = this.ticketDao.save(ticket);

        defaultPermissionTable.grantDefaultPermission(ticket.getId());
        defaultPermissionTable.denyDefaultPermission(ticket.getId());

        return newTicket;
    }

    @Transactional
    @LogOperation(tag = "TICKET_UPDATE", inputArgs = {"ticket"})
    //@PreAuthorize("hasPermission(#id,'com.uniroma2.isssrbackend.entity.ticket.Ticket', 'WRITE') or hasAuthority('ROLE_ADMIN')")
    public Ticket updateById(@NotNull Long id, @NotNull Ticket updatedTicket) throws EntityNotFoundException {
        Ticket toBeUpdatedTicket = getTicketById(id);

        toBeUpdatedTicket.updateTicket(updatedTicket);
        return ticketDao.save(toBeUpdatedTicket);
    }

    @Transactional
    @PostAuthorize("hasPermission(returnObject, 'READ') or returnObject == null or hasAuthority('ROLE_ADMIN')")
    public Ticket getTicketById(@NotNull Long id) throws EntityNotFoundException {
        Optional<Ticket> ticket = this.ticketDao.findById(id);

        if (!ticket.isPresent()) {
            throw new EntityNotFoundException("TICKET NOT FOUND");
        }

        return ticket.get();

        //return ticketDao.getOne(id);
    }

    @Transactional
    @PostFilter("filterObject?.customer?.username == principal?.username " +
            "or hasAuthority('ROLE_TEAM_MEMBER') or hasAuthority('ROLE_ADMIN')")
    public List<Ticket> getAllTickets() {
        return this.ticketDao.findAll();
    }

    @Transactional
    public boolean existsById(Long id) {
        return this.ticketDao.existsById(id);
    }

    @Transactional
    public long count() {
        return this.ticketDao.count();
    }

    @Transactional
    @LogOperation(tag = "TICKET_DELETE")
    @PostAuthorize("hasPermission(returnObject,'DELETE') or returnObject == null or hasAuthority('ROLE_ADMIN')")
    public boolean deleteTicketById(@NotNull Long id) throws EntityNotFoundException {
        Ticket ticket = getTicketById(id);
        ticket.delete();
        this.ticketDao.save(ticket);
        defaultPermissionTable.removeDefaultPermission(id);
        return true;
    }

    @Transactional
    @PostAuthorize("hasAuthority('ROLE_ADMIN')")
    public void deleteAll() {
        this.ticketDao.deleteAll();
    }

/*
    @Transactional
    @PostFilter("filterObject.assignee.username == principal.username " +
            "or hasAuthority('ROLE_TEAM_COORDINATOR')  or hasAuthority('ROLE_ADMIN')")
    public List<Ticket> getAllTicketsByAssistant(User assistant) {
        return ticketDao.findAllByAssignee(assistant);
    }
*/
    @Transactional
    @PostFilter("filterObject.customer.username == principal.username " +
            "or hasAuthority('ROLE_TEAM_MEMBER') or hasAuthority('ROLE_ADMIN')")
    public List<Ticket> getTicketsByCustomer(@NotNull User customer) {
        return ticketDao.findAllByCustomer(customer);
    }


    @Transactional
    @PostFilter("hasPermission(returnObject, 'READ') or returnObject == null or hasAuthority('ROLE_ADMIN')")
    public List<Ticket> getTicketsByAssignee(@NotNull Long assigneeID) throws EntityNotFoundException {
        User assignee = userController.findById(assigneeID);
        return ticketDao.findAllByAssignee(assignee);
    }

    @Transactional
    public List<Ticket> getTicketsByStatus(TicketStatus ticketTicketStatus){
        return ticketDao.getTicketByStatus(ticketTicketStatus);
    }
    
    /* Progetto gestione relazioni e pianificazione */




    @Transactional
    public List<Ticket> findTicketsByCustomer(User user){
        List<Ticket> tickets = ticketDao.findByCustomer(user);
        return tickets;
    }


    @Transactional
    public List<Ticket> findTicketBySameTicket(Ticket ticket){
        List<Ticket> tickets = ticketDao.findBySameTicket(ticket);
        return tickets;
    }

    @Transactional
    public List<Ticket> addDependentTicket( @NotNull Long ID, @NotNull Long dependentID) throws NotFoundEntityException {
        Ticket ticketMain = ticketDao.getOne(ID);
        Ticket dependentTicket = ticketDao.getOne(dependentID);
        List<Ticket> cycle = new ArrayList<>();

        if (ticketMain == null || dependentTicket==null)
            throw new NotFoundEntityException();
        if(ticketMain.isAlreadyDependent(dependentTicket))
            return cycle;
        //check if there is no-cycle
        if(dependentTicket.isAcycle(ticketMain, cycle).isEmpty()) {
            ticketMain.addDependentTickets(dependentTicket);
            ticketDao.save(ticketMain);
            dependentTicket.addCount();
            ticketDao.save(dependentTicket);
            return cycle;

        }
        else return cycle;
    }

    @Transactional
    public Ticket releaseTicket(@NotNull  Long id, @NotNull Ticket ticket) throws NotFoundEntityException {
        Ticket ticketReleased = ticketDao.getOne(id);
        if (ticketReleased == null )
            throw new NotFoundEntityException();
        ticketReleased.update(ticket);
        ticketDao.save(ticketReleased);
        Set<Ticket> dependents = ticketReleased.decreaseDependents();
        for(Ticket t: dependents)
            ticketDao.save(t);
        return ticketReleased;

    }

    @Transactional
    public Ticket addRegression(@NotNull Long id,@NotNull Long idGenerator) throws NotFoundEntityException {
        Ticket ticketRegression = ticketDao.getOne(id);
        Ticket ticketGenerator = ticketDao.getOne(idGenerator);
        if (ticketRegression == null )
            throw new NotFoundEntityException();
        if (ticketGenerator == null )
            throw new NotFoundEntityException();
        ticketRegression.addRegression(ticketGenerator);
        return ticketDao.save(ticketRegression);

    }

//------------  Relation  -------------------------



    @Transactional
    public List<Ticket> findTicketNoRelation() {
        List<Ticket> tickets = ticketDao.findDistinctBySameTicketIsNullAndDependentTicketsIsNullAndCountDependenciesIsNullAndRegressionTicketsGeneratorIsNull();
        return tickets;
    }


    @Transactional
    public List<Ticket> findTicketDependency() {
        List<Ticket> tickets = ticketDao.findDistinctByDependentTicketsIsNotNullOrCountDependenciesIsNotNull();
        return tickets;
    }

    @Transactional
    public List<Ticket> findTicketForCreateEquality() {
        List<Ticket> tickets = ticketDao.findDistinctBySameTicketIsNull();
        return tickets;
    }

    @Transactional
    public List<Ticket> findTicketForCreateDependency() {
        List<Ticket> tickets = ticketDao.findDistinctBySameTicketIsNullAndRegressionTicketsGeneratorIsNull();
        return tickets;
    }

    @Transactional
    public List<Ticket> findTicketForCreateRegression() {
        List<Ticket> tickets = ticketDao.findDistinctByCurrentTicketStatus(TicketStatus.CLOSED);

        return tickets;
    }

//-------------- Escalation ----------------------------------------------

    @Transactional
    //NB: per stampare la coda dei ticket in pending
    public List<Ticket> findTicketInQueue (){
        List<Ticket> tickets = ticketDao.findDistinctByCurrentTicketStatusOrderByRankDesc(TicketStatus.PENDING);

        return tickets;
    }


//---------------------------GANTT----------------------------------------------


    @Transactional
    //NB: per ottenere tutti i ticket assegnati ad un team
    public List<Ticket> findTicketByTeam(String teamName) throws EntityNotFoundException {
        Optional<Team> team = teamDao.findByName(teamName);
        if (!team.isPresent()) {
            throw new EntityNotFoundException("Team not found");
        }
        List<Ticket> tickets = ticketDao.findByTeamAndCurrentTicketStatusIsNotAndCurrentTicketStatusIsNot(team.get(),
                TicketStatus.ACCEPTANCE, TicketStatus.CLOSED);
        return  tickets;


    }


    @Transactional
    public List<Ticket> findTicketForGanttByTeam(String teamName) throws EntityNotFoundException {
        Optional<Team> team = teamDao.findByName(teamName);
        if (!team.isPresent()) {
            //return new ArrayList<Ticket>();
            throw new EntityNotFoundException("TEAM_NOT_FOUND");
        }
        List<Ticket> tickets = ticketDao.findByTeamAndCurrentTicketStatusIsNotAndCurrentTicketStatusIsNot(team.get(),
                TicketStatus.ACCEPTANCE, TicketStatus.CLOSED);
        return tickets;

    }



    @Transactional
    public List<Ticket> findFatherTicket(@NotNull Long ticketId) {
        Set<Ticket> tickets = new HashSet<>();
        tickets.add(ticketDao.getOne(ticketId));
        List<Ticket> fatherTickets = ticketDao.findDistinctByDependentTicketsContains(tickets); // ticket da cui dipende il ticket dato
        // I ticket che sono già stati chiusi non devono comparire in questa lista
        fatherTickets.removeIf(ticket -> ticket.getCurrentTicketStatus() == TicketStatus.CLOSED);
        return fatherTickets;
    }

    @Transactional
    public Ticket updateTicketDifficulty(Long id, TicketDifficulty difficulty) throws EntityNotFoundException {
        Ticket ticket = getTicketById(id);
        ticket.setDifficulty(difficulty);

        return ticketDao.save(ticket);
    }





    /**
     * Assegna un Ticket a un TeamLeader
     *
     * @param ticketID l'id del ticket da assegnare
     * @param userId l'id del team leader a cui assegnare il ticket.
     *
     */
    @Transactional
    public Ticket assignTicket(@NotNull Long ticketID, @NotNull Long userId) throws EntityNotFoundException {
        Ticket assignedTicket  = this.getTicketById(ticketID);
        User teamMember = userController.findById(userId);
        assignedTicket.setAssignee(teamMember);
        return ticketDao.save(assignedTicket);
    }
    
    /**
     * Metodo per inserire un commento in un ticket
     *
     * @param ticketID ID del ticket da commentare
     * @param ticketComment commento da allegare al ticket
     * @return il ticket con allegato il commento
     */
    @Transactional
    public Ticket insertComment(Long ticketID, Long userID, TicketComment ticketComment) throws EntityNotFoundException {

        Ticket ticket = getTicketById(ticketID);
        User user = userController.findById(userID);
        ticketComment.setEventGenerator(user);
        ticket.getComments().add(ticketComment);
        ticketDao.save(ticket);

        return ticket;
    }


    /**
     * Metodo per cambiare lo stato di un ticket.
     *
     *
     * @param ticketID id del ticket da modificare
     * @param action String che identifica l'azione da intraprendere come configurato nell file XML.
     * @return il ticket modificato
     */
    @Transactional
    public Ticket changeStatus(Long ticketID, String action) throws EntityNotFoundException {

        Ticket ticket = getTicketById(ticketID);
        //ticket.setCreationTimestamp(Instant.now());
        ticket.getStateMachine().ProcessFSM(action);
        TicketStatus ticketStatus = TicketStatus.getEnum(ticket.getStateMachine().getCurrentState());
        if(ticketStatus ==null)
            return null;
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd/mm/yyyy");
        if (ticketStatus == TicketStatus.PENDING)
            ticket.setDatePendingStart(dateFormat.format(date));
        else if (ticketStatus == TicketStatus.EXECUTION)
            ticket.setDateExecutionStart(dateFormat.format(date));
        else if (ticketStatus == TicketStatus.CLOSED)
            ticket.setDateEnd(dateFormat.format(date));
        ticket.setCurrentTicketStatus(ticketStatus);
        ticket.setTTL(ticketStatus.getTTL());
        ticket.setStateCounter(System.currentTimeMillis());
        FSM stateMachine = ticket.getStateMachine();
        ticket.setStateInformation(stateMachine.getStateInformation(ticketStatus.toString()));
        return ticketDao.save(ticket);
    }

    /**
     * Metodo per cambiare lo stato di un ticket e il Resolver User del Ticket.
     *
     * @param newAssigneeId id dell'internal user da cambiare
     * @param ticketID id del ticket da assegnare
     * @param action String che identifica l'azione da intraprendere come configurato nell file XML.
     * @return il Ticket aggiornato con lo stato e il resolver user modificati
     */
    @Transactional
    public Ticket changeStatusAndAssignee(Long ticketID, String action, Long newAssigneeId) throws EntityNotFoundException {

        Ticket ticket = getTicketById(ticketID);
        User newAssignee = null;
        User exAssignee = ticket.getAssignee();
        ticket.setCustomerState(false);
        /*
        if(newAssigneeId.equals(SystemRole.TeamLeader.toString())){

            if(!exResolverUser.getClass().equals(TeamLeader.class)){

                if(exResolverUser.getClass().equals(TeamMember.class)){

                    TeamMember exTeamMember = (TeamMember) exResolverUser;
                    registeredUser = exTeamMember.getTeam().getTeamLeader();

                }
                if(exResolverUser.getClass().equals(TeamCoordinator.class)){

                    registeredUser = registeredUserController.getRandomTeamLeader();


                }
            }
            else {

                registeredUser = exResolverUser;
            }
        }
        else if(internalUserID.equals(SystemRole.TeamMember.toString())){
            if(!exResolverUser.getClass().equals(TeamMember.class)){
                if(exResolverUser.getClass().equals(TeamCoordinator.class)){
                    registeredUser = registeredUserController.getRandomTeamMember();

                }
                if(exResolverUser.getClass().equals(TeamLeader.class)){
                    registeredUser = exResolverUser;

                }
            }
            else registeredUser = exResolverUser;
        }
        else if(internalUserID.equals(SystemRole.TeamCoordinator.toString())){
            if(!exResolverUser.getClass().equals(TeamCoordinator.class)){
                registeredUser = registeredUserController.getTeamCoordinator();
            }
            else registeredUser = exResolverUser;
        }
        else {
            Long iUserID = Long.parseLong(internalUserID);
            if(iUserID != 0 ) {

                registeredUser = registeredUserController.findRegisteredUserById(iUserID);
                ticket.setResolverUser((InternalUser) registeredUser);
            }
            else {
                ticket.setCustomerStatus(true);
                registeredUser = exResolverUser;
            }
        }
        ticket.setAssignee((InternalUser) newAssignee);
        ticketDao.insertUser(ticket);
*/

        if (newAssigneeId != 0) {
            newAssignee = userController.findById(newAssigneeId);
            ticket.setAssignee(newAssignee);
        }
        ticketDao.save(ticket);

        return changeStatus(ticketID, action);
    }




    @Transactional
    public Ticket updateTicketPriority(Long id, TicketPriority ticketPriority) throws EntityNotFoundException {
        Ticket ticket = getTicketById(id);
        ticket.setActualPriority(ticketPriority);

        return ticketDao.save(ticket);
    }


    @Transactional
    public Ticket updateTicketPriorityAndActualType(Long id, TicketPriority ticketPriority, String actualType) throws EntityNotFoundException {
        Ticket ticket = getTicketById(id);
        ticket.setActualPriority(ticketPriority);
        ticket.setActualType(actualType);
        return ticketDao.save(ticket);
    }

    @Transactional
    public List<GanttDay> getPlanningAndChangeTicketState(@NotNull Ticket ticket, @NotNull String username,
                                                  @NotNull String firstDay, @NotNull Integer duration,
                                                  @NotNull Long ticketId,String action, Long internalUserID) throws DependeciesFoundException, EntityNotFoundException {
        List<GanttDay> ganttDays = new ArrayList<>();

        Team team = teamController.findAllTeamByPerson(username).get(0);
        Ticket ticketToUpdate = ticketDao.getOne(ticketId);

        GregorianCalendar first = ParseDate.parseGregorianCalendar(firstDay);

        //check dependencies
        Set<Ticket> tickets = new HashSet<>();
        tickets.add(ticketToUpdate);
        List<Ticket> fatherTickets = ticketDao.findDistinctByDependentTicketsContains(tickets);
        for (Ticket father : fatherTickets) {
            if (father.getCurrentTicketStatus() != TicketStatus.CLOSED) { // solo se il ticket padre non è stato chiuso è considerato come precedente
                Integer durationFather = ticketDao.findDurationByTicket(father);
                if (durationFather == null) {
                    throw new DependeciesFoundException();
                }
                GregorianCalendar dateExecStartFather = ParseDate.parseGregorianCalendar(ticketDao.findDateExecutionByTicket(father));
                dateExecStartFather.add(Calendar.DAY_OF_MONTH, durationFather);
                if (dateExecStartFather.compareTo(first) > 0) {
                    throw new DependeciesFoundException();
                }
            }
        }

        //check availability for all day
        for (int i = 0; i < duration; i++) {
            String currentDay = ParseDate.gregorianCalendarToString(first);
            KeyGanttDay keyGanttDay = new KeyGanttDay(currentDay, team);
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

        for (int i = 0; i < duration; i++) {
            String currentDay = ParseDate.gregorianCalendarToString(first);
            ganttDayController.updateGanttDay(currentDay, ticketToUpdate, team);
            first.add(Calendar.DAY_OF_MONTH, 1);
        }

        ticket.setTeam(team);



        if (internalUserID != 0) {
            User newAssignee = userController.findById(internalUserID);
            ticketToUpdate.setAssignee(newAssignee);
        }

        //Ticket ticket = getTicketById(ticketID);
        ticketToUpdate.getStateMachine().ProcessFSM(action);
        TicketStatus ticketStatus = TicketStatus.getEnum(ticketToUpdate.getStateMachine().getCurrentState());
        if(ticketStatus ==null)
            return null;
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd/mm/yyyy");
        if (ticketStatus == TicketStatus.PENDING)
            ticketToUpdate.setDatePendingStart(dateFormat.format(date));
        else if (ticketStatus == TicketStatus.EXECUTION)
            ticketToUpdate.setDateExecutionStart(dateFormat.format(date));
        else if (ticketStatus == TicketStatus.CLOSED)
            ticketToUpdate.setDateEnd(dateFormat.format(date));
        ticketToUpdate.setCurrentTicketStatus(ticketStatus);
        ticketToUpdate.setTTL(ticketStatus.getTTL());
        ticketToUpdate.setStateCounter(System.currentTimeMillis());
        FSM stateMachine = ticketToUpdate.getStateMachine();
        ticketToUpdate.setStateInformation(stateMachine.getStateInformation(ticketStatus.toString()));



        ticketToUpdate.update(ticket);
        ticketDao.save(ticketToUpdate);

        return ganttDays;




       // return ticketDao.save(ticketToUpdate);
    }

    /*########################################################################################################*/

    // createEquivalentRelation crea una relazione di equivalenza tra i ticket aventi idA e idB
    @Transactional
    public Ticket createEquivalentRelation(Long idA, long idB) {

        Ticket ticketA = ticketDao.findTicketById(idA);
        Ticket ticketB = ticketDao.findTicketById(idB);

        //Se gli ID coincidono non si fa nulla e si ritorna il ticket A
        if (idA == idB){
            return ticketA;
        }

        //A e B non fanno parte di una relazione: A diventa primario e B secondariod di A
        else if (ticketA.isNotInEquivalenceRelation() && ticketB.isNotInEquivalenceRelation()) {
            ticketA.setEquivalencePrimary(ticketA);
            ticketA.addEquivalentTicket(ticketB);
            ticketB.setEquivalencePrimary(ticketA);
        }

        //A è primario e B non fa parte di una relazione: B diventa secondario di A
        else if (ticketA.isEquivalencePrimary() && ticketB.isNotInEquivalenceRelation()) {
            ticketA.addEquivalentTicket(ticketB);
            ticketB.setEquivalencePrimary(ticketA);
        }

        //A non fa parte di una relazione e B è primario: A diventa secondario di B
        else if (ticketA.isNotInEquivalenceRelation() && ticketB.isEquivalencePrimary()) {
            ticketB.addEquivalentTicket(ticketA);
            ticketA.setEquivalencePrimary(ticketB);
        }

        //A è secondario e B non fa parte di una relazione: B diventa secondario del primario di A
        else if (ticketA.isEquivalenceSecondary() && ticketB.isNotInEquivalenceRelation()) {
            ticketB.setEquivalencePrimary(ticketA.getEquivalencePrimary());
            ticketA.getEquivalencePrimary().addEquivalentTicket(ticketB);
        }

        //A non fa parte di una relazione e B è secondario: A diventa secondario del primario di B
        else if (ticketA.isNotInEquivalenceRelation() && ticketB.isEquivalenceSecondary()) {
            ticketA.setEquivalencePrimary(ticketB.getEquivalencePrimary());
            ticketB.getEquivalencePrimary().addEquivalentTicket(ticketA);
        }

        // A e B sono entrambi primari: B diventa secondario di A
        else if (ticketA.isEquivalencePrimary() && ticketB.isEquivalencePrimary()){
            ticketB.setEquivalencePrimary(ticketA);
            ticketA.addEquivalentTicket(ticketB);
            for (Ticket t : ticketB.getEquivalentTickets()){
                ticketA.addEquivalentTicket(t);
                t.setEquivalencePrimary(ticketA);
            }
            Iterator<Ticket> iterator = ticketB.getEquivalentTickets().iterator();
            while (iterator.hasNext()) {
                iterator.next();
                iterator.remove();
            }

        }

        //A è secondario e B primario: se il primario di A è B non si fa nulla altrimenti si crea una relazione di
        // equivalenza tra il primario di A e B
        else if (ticketA.isEquivalenceSecondary() && ticketB.isEquivalencePrimary()){
            if (ticketA.getEquivalencePrimary().equals(ticketB)){
                return ticketA;
            } else {
                Long idPrimaryOfA = ticketA.getEquivalencePrimary().getId();
                this.createEquivalentRelation(idPrimaryOfA, idB);
            }
        }

        //A è primario e B secondario: se il primario di B è A non si fa nulla altrimenti si crea una relazione di
        // equivalenza tra il primario di B ed A
        else if (ticketA.isEquivalencePrimary() && ticketB.isEquivalenceSecondary()){
            if (ticketB.getEquivalencePrimary().equals(ticketA)){
                return ticketA;
            } else{
                Long idPrimaryOfB = ticketB.getEquivalencePrimary().getId();
                this.createEquivalentRelation(idA, idPrimaryOfB);
            }
        }

        //A e B sono entrambi secondari: se il loro primale coincide non si fa nulla altrimenti si crea una relazione
        // di equivalenza tra il primario di A e il primario di B
        else if (ticketA.isEquivalenceSecondary() & ticketB.isEquivalenceSecondary()){
            if (ticketA.getEquivalencePrimary().equals(ticketB.getEquivalencePrimary())){
                return ticketA;
            } else {
                Long idPrimaryOfA = ticketA.getEquivalencePrimary().getId();
                Long idPrimaryOfB = ticketB.getEquivalencePrimary().getId();
                this.createEquivalentRelation(idPrimaryOfA, idPrimaryOfB);
            }
        }

        ticketDao.save(ticketA);
        ticketDao.save(ticketB);
        return ticketA;
    }

    @Transactional
    /* getEquivalentTicketTitles restituisce una lista di stringhe corrispondenti ai nomi dei ticket equivalenti
     * a quello passato come parametro. Nello specifico ciascuna stringa è costituita da <id_ticket>-<nome_ticket>*/
    public List<String> getEquivalentTickets(Long ticketId) {

        List<String> titles = new ArrayList<>();
        Ticket ticket = ticketDao.findTicketById(ticketId);

        // Se il ticket e primario si restituiscono i nomi dei ticket nella lista equivalentTickets
        if (ticket.isEquivalencePrimary()){
            for (Ticket t : ticket.getEquivalentTickets()){
                titles.add("" + t.getId() + "-" + t.getTitle());
            }

            // Se il ticket è secondario si restituiscono i nomi dei ticket nella lista equivalentTickets del primario
        } else if (ticket.isEquivalenceSecondary()) {
            for (Ticket t : ticket.getEquivalencePrimary().getEquivalentTickets()) {
                if (t.getId() != ticketId){
                    titles.add("" + t.getId() + "-" + t.getTitle());
                }
            }
        }
        return titles;
    }
}