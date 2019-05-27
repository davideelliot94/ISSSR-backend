package com.isssr.ticketing_system.controller;

import com.isssr.ticketing_system.entity.Company;
import com.isssr.ticketing_system.dao.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CompanyController {

    @Autowired
    private CompanyRepository companyRepository;

    @Transactional
    public Company save(Company company) {
        return this.companyRepository.save(company);
    }

    @Transactional
    public Optional<Company> findByName(String name) {
        return this.companyRepository.findByName(name);
    }

    @Transactional
    public Optional<Company> findById(Long id) {
        return this.companyRepository.findById(id);
    }

    @Transactional
    public Iterable<Company> findAll() {
        return this.companyRepository.findAll();
    }

    @Transactional
    public boolean existsByName(String name) {
        return this.companyRepository.existsByName(name);
    }

    @Transactional
    public boolean existsByDomain(String domain) {
        return this.companyRepository.existsByDomain(domain);
    }
}
