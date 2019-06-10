package com.isssr.ticketing_system.dao;

import com.isssr.ticketing_system.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface UserDao extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(@Param("username") String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Page<User> findAll(Pageable pageable);

    //Page<User> findByEmailContaining(String email, Pageable pageable);

    List<User> findByEmailContaining(String email);

    List<User> findByIdIn(List<Long> listId);

    @Query("select u from User u where role = com.isssr.ticketing_system.enumeration.UserRole.TEAM_COORDINATOR")
    List<User> getTeamCoordinators();

    @Query("select count(*) from User where role <> com.isssr.ticketing_system.enumeration.UserRole.CUSTOMER")
    int countUserNotCustomer();

    @Query("select tl from User tl where role <> com.isssr.ticketing_system.enumeration.UserRole.CUSTOMER")
    List<User> findAllNotCustomer();

    //@Query("select tl from User tl where role = com.isssr.ticketing_system.enumeration.UserRole.TEAM_LEADER and tl.team_leader_id <> null ")
    @Query("select tl from User tl where role = com.isssr.ticketing_system.enumeration.UserRole.TEAM_LEADER")
    List<User> getListEmployedTeamLeader();

    @Query("select tl from User tl where role = com.isssr.ticketing_system.enumeration.UserRole.TEAM_MEMBER and tl.team <> null ")
    List<User> getListEmployedTeamMember();

    @Query("select MAX (id) from User")
    Long getMaxId();

    @Query("select distinct s.teamMembers from ScrumTeam s where s.id=:scrumTeamId")
    ArrayList<User> getMembersBySTId(@Param("scrumTeamId") Long scrumTeamId);

    @Query("select distinct u from ScrumTeam s join User u on s.scrumMaster.id = u.id where s.id=:scrumTeamId")
    User getScrumMasterBySTId(@Param("scrumTeamId") Long scrumTeamId);

    @Query("select distinct u from ScrumTeam s join User u on s.productOwner.id = u.id where s.id=:scrumTeamId")
    User getProductOwnerBySTId(@Param("scrumTeamId") Long scrumTeamId);

}
