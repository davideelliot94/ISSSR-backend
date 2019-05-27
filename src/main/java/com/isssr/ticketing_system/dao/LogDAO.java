package com.isssr.ticketing_system.dao;

import com.isssr.ticketing_system.entity.RequestLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogDAO extends JpaRepository<RequestLog, Long> {

}
