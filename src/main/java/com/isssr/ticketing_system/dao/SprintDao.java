package com.isssr.ticketing_system.dao;

import com.isssr.ticketing_system.entity.Sprint;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SprintDao extends JpaRepository<Sprint, Long> {
}
