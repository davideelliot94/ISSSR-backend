package com.isssr.ticketing_system.dao;


import com.isssr.ticketing_system.entity.Sprint;
import com.isssr.ticketing_system.entity.Target;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SprintDao extends JpaRepository<Sprint, Long> {

    List<Sprint> findAllByProduct(Target product);
    Sprint findFirstByProductAndNumber(Target target, Integer sprintNumber);
}
