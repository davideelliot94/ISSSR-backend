package com.isssr.ticketing_system.dao;

import com.isssr.ticketing_system.entity.StateMachine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StateMachineDao extends JpaRepository<StateMachine,Long> {


}
