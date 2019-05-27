package com.isssr.ticketing_system.dao;

import com.isssr.ticketing_system.acl.Authority;
import com.isssr.ticketing_system.acl.AuthorityName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthorityDAO extends JpaRepository<Authority, Long> {

    Authority findBySid(AuthorityName name);

    List<Authority> findByIdIn(List<Long> listId);

    List<Authority> findByPrincipalEquals(Integer principal);

    Authority save(Authority authority);
}
