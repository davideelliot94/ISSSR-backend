package com.isssr.ticketing_system.dao;

import com.isssr.ticketing_system.entity.ACLRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;


public interface ACLRecordDAO extends JpaRepository<ACLRecord, Long> {

    ACLRecord findBySid(@Param("sid") String sid);
}
