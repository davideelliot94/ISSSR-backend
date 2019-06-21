package com.isssr.ticketing_system.dao;


import com.isssr.ticketing_system.entity.Sprint;
import com.isssr.ticketing_system.entity.Target;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SprintDao extends JpaRepository<Sprint, Long> {

    Sprint findFirstByProductAndNumber(Target target, Integer sprintNumber);
    Sprint findFirstByProductOrderByNumberDesc(Target product);

}
