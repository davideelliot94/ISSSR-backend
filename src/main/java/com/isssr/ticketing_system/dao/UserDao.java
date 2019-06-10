package com.isssr.ticketing_system.dao;

import com.isssr.ticketing_system.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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


    //@Query("select tl from User tl where role = com.isssr.ticketing_system.enumeration.UserRole.TEAM_LEADER and tl.team_leader_id <> null ")
    @Query("select tl from User tl where role = com.isssr.ticketing_system.enumeration.UserRole.TEAM_LEADER")
    List<User> getListEmployedTeamLeader();

    @Query("select tl from User tl where role = com.isssr.ticketing_system.enumeration.UserRole.TEAM_MEMBER and tl.team <> null ")
    List<User> getListEmployedTeamMember();

}
