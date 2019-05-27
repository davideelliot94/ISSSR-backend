package com.isssr.ticketing_system.dao;

import com.isssr.ticketing_system.entity.SoftDelete.SoftDelete;
import com.isssr.ticketing_system.entity.SoftDelete.SoftDeleteKind;
import com.isssr.ticketing_system.entity.Team;
import com.isssr.ticketing_system.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface TeamDao extends JpaRepository<Team, Long> {
    Optional<Team> findByName(String name);

    boolean existsByName(String name);

    Page<Team> findAll(Pageable pageable);

    /* Progetto gestione relazioni e pianificazione */

    @Query("select t.teamMembers from Team t where t = :team")
    List<User> findTeamMembersByTeam(@Param("team") Team team);

    //List<Team> findAllByTeamMembersContainsOrTeamLeaderOrTeamCoordinator(Set<User> teamMembers, User teamLeader, User teamCoordinator);


    /**
     * Metodo Query che seleziona tutti i TeamMember di un dato Team
     *
     * @param id  ID del team di cui occorre trovare i TeamMember
     * @return lista di TeamMember del Team.
     */
    @SuppressWarnings("all")
    @Query("select u.teamMembers from Team u where u.id = :id")
    Collection<User> getTeamMembersByTeamId(@Param("id") Long id);


    /**
     * Metodo Query che seleziona tutti i TeamMember di un dato Team dato il TeamLeader
     *
     * @param id del TeamLeader di cui interessano gli "Assistenti"
     * @return Lista dei TeamMember del Team.
     */
    @SuppressWarnings("all")
    @Query("select u.teamMembers from Team u where u.teamLeader.id = :id")
    Collection<User> getTeamMembersByTeamLeaderId(@Param("id") Long id);

    @SoftDelete(SoftDeleteKind.NOT_DELETED)
    List<Team> findAllByTeamMembersContainsOrTeamLeaderOrTeamCoordinator(Set<User> teamMembers, User teamLeader, User teamCoordinator);
}
