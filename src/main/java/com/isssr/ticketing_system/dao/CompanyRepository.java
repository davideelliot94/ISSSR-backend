package com.isssr.ticketing_system.dao;

import com.isssr.ticketing_system.entity.Company;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends CrudRepository<Company, Long> {
    Optional<Company> findByName(String name);

    Optional<Company> findById(Long id);

    boolean existsByName(String name);

    boolean existsByDomain(String domain);

}