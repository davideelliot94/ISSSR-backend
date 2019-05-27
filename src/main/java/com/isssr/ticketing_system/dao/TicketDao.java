package com.isssr.ticketing_system.dao;

import com.isssr.ticketing_system.entity.Team;
import com.isssr.ticketing_system.entity.Ticket;
import com.isssr.ticketing_system.entity.User;
import com.isssr.ticketing_system.enumeration.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface TicketDao extends JpaRepository<Ticket, Long> {

    //Page<Ticket> findAll(Pageable pageable);

    //Page<Ticket> findByTitleContaining(String title, Pageable pageable);

    List<Ticket> findByTitleContaining(String title);

    List<Ticket> findAllByCustomer(User customer);

    List<Ticket> findAllByAssignee(User assignee);



    /* Progetto Gestione relazioni e pianificazione */

    //NB: nel DAO è possibile creare dei metodi di ricerca della classe associata al DAO (Ticket in questo caso)
    // sulla base di condizioni definibili sugli attributi (=, !=, isNull, isNotNull, Contains, etc.)
    List<Ticket> findByCustomer(User customer);

    List<Ticket> findBySameTicket(Ticket ticket);

    //Ticket for create a dependency relation
    List<Ticket> findDistinctByDependentTicketsIsNotNullOrCountDependenciesIsNotNull();
    //Ticket for create a regression relation
    List<Ticket> findDistinctByCurrentTicketStatus(TicketStatus currentTicketStatus);
    List<Ticket> findDistinctByCurrentTicketStatusOrderByRankDesc(TicketStatus currentTicketStatus);
    List<Ticket> findDistinctBySameTicketIsNullAndDependentTicketsIsNullAndCountDependenciesIsNullAndRegressionTicketsGeneratorIsNull();

    @Query("select t.id from Ticket t where t= :ticket")
        //NB: @Query permette di specificare una query in JPQL (query SQL sulle classi java). E' possibile specificare dei parametri
        // di input della query con ":parametro", e richiamati nell'input del metodo del DAO con la notazione @Param("parametro") ClasseParametro nomeParametro
    Long getIDByTicket(@Param("ticket") Ticket ticket);

    List<Ticket> findDistinctBySameTicketIsNull();

    List<Ticket> findDistinctBySameTicketIsNullAndRegressionTicketsGeneratorIsNull();


    //-----------------------------GANTT-------------------------------
    List<Ticket> findByTeamAndCurrentTicketStatusIsNotAndCurrentTicketStatusIsNot(Team team, TicketStatus currentTicketStatus, TicketStatus currentTicketStatus2);

    @Query("select t.difficulty from Ticket t where t = :ticket")
    Double findDifficultyByTicket(@Param("ticket") Ticket ticket);

    @Query("select t.dependentTickets from Ticket t where  t = :ticket")
    List<Ticket> getDependentTicketByTicket(@Param("ticket") Ticket ticket);

    List<Ticket> findDistinctByDependentTicketsContains(Set<Ticket> dependentTickets);

    @Query("select t.durationEstimation from Ticket t where t = :ticket")
    Integer findDurationByTicket(@Param("ticket") Ticket ticket);

    @Query("select t.dateExecutionStart from Ticket t where t = :ticket")
    String findDateExecutionByTicket(@Param("ticket") Ticket ticket);

    /**
     * To search the ticket opened by a specified user username
     *
     * @param customerID id del customer di cui cercare i ticket
     * @return All tickets opened by a user
     */
    @Query("select t from Ticket t where t.customer.id = :customerID")
    List<Ticket> getTicketByCustomer(@Param("customerID") Long customerID);

    /**
     * To search the ticket assigned to a specified TeamMember username
     *
     * @param assigneeID id del TeamLeader di cui cercare i ticket
     * @return All tickets assigned to a TeamMember
     */
    @Query("select t from Ticket t where t.assignee.id = :assigneeID")
    List<Ticket> getTicketByAssignee(@Param("assigneeID") Long assigneeID);


    /**
     * Per ricercare un ticket in una specifica fase del suo WorkFlow
     *
     * @param ticketStatus Stato dei Ticket da ricercare nel DB.
     * @return Tutti i ticket nello specifico ticketTicketStatus
     */
    @Query("select t from Ticket  t where t.currentTicketStatus = :ticketStatus")
    List<Ticket> getTicketByStatus(@Param("ticketStatus") TicketStatus ticketStatus);



    @Query("select count (distinct t.id) from Ticket t where t.currentTicketStatus = ?1")
    Integer numberOfStatusTickets(TicketStatus status);

    Ticket findTicketById(Long id);
}
