package com.isssr.ticketing_system.dao;

import com.isssr.ticketing_system.entity.Relation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RelationDao extends JpaRepository<Relation,String> {

/*    @Query("select r.cyclic from Relation r where r = :relation ")
    Boolean findCyclicByRelation(@Param("relation") Relation relation);*/

    @Query("select r.cyclic from Relation r where r = :relation ")
    Boolean findCyclicByRelation(@Param("relation") Relation relation);
}
