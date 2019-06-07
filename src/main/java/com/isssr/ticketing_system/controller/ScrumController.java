package com.isssr.ticketing_system.controller;

import com.isssr.ticketing_system.dao.ScrumDao;
import com.isssr.ticketing_system.dao.TargetDao;
import com.isssr.ticketing_system.entity.ScrumTeam;
import com.isssr.ticketing_system.entity.Target;
import com.isssr.ticketing_system.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
public class ScrumController {

    @Autowired
    private ScrumDao scrumDao;

    @Autowired
    private TargetDao targetDao;

    @Transactional
    public ArrayList<ScrumTeam> getScrumTeamList() {

        return scrumDao.getScrumTeamList();
    }

    @Transactional
    public ArrayList<User> getMembersBySTId(Long id) {

        return scrumDao.getMembersBySTId(id);
    }

    @Transactional
    public User getScrumMasterBySTId(Long id) {

        return scrumDao.getScrumMasterBySTId(id);
    }

    @Transactional
    public User getProductOwnerBySTId(Long id) {

        return scrumDao.getProductOwnerBySTId(id);
    }

    @Transactional
    public void assignProductToST(Long tid, Long pid) {

        Target target = targetDao.getOne(pid);
        target.setScrumTeam(scrumDao.getOne(tid));
        targetDao.save(target);
    }
}
