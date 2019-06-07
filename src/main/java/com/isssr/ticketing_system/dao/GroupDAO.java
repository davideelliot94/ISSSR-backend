package com.isssr.ticketing_system.dao;

import com.isssr.ticketing_system.acl.groups.Group;
import com.isssr.ticketing_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupDAO extends JpaRepository<Group, Long> {

    List<Group> findAllByMembersEquals(User userType);

    Group findByName(String name);

    @Query("select g.id from Group g where g.name LIKE:role")
    Long getGroupByRole(@Param("role") String role);
}
