package com.isssr.ticketing_system.controller;

import com.isssr.ticketing_system.entity.Setup;
import com.isssr.ticketing_system.dao.SetupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class SetupController {

    @Autowired
    private SetupRepository setupRepository;

    @Transactional
    public Setup save(Setup setup) {
        return this.setupRepository.save(setup);
    }

    @Transactional
    public Iterable<Setup> findAll() {
        return this.setupRepository.findAll();
    }

    @Transactional
    public Optional<Setup> findById(Setup setup) {
        return this.setupRepository.findById(setup);
    }

    @Transactional
    public boolean existsBySetup(boolean setup) {
        return this.setupRepository.existsBySetup(setup);
    }
}
