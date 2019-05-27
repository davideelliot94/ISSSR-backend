package com.isssr.ticketing_system.controller;

import com.isssr.ticketing_system.acl.Authority;
import com.isssr.ticketing_system.acl.AuthorityName;
import com.isssr.ticketing_system.dao.AuthorityDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthorityController {

    @Autowired
    private AuthorityDAO authorityDAO;

    @Transactional
    public List<Authority> getAllAuthorities() {
        return authorityDAO.findByPrincipalEquals(0);
    }

    @Transactional
    public Authority save(Authority authority) {
        return authorityDAO.save(authority);
    };

    @Transactional
    public Authority getBySid(AuthorityName sid) { return authorityDAO.findBySid(sid); }
}
