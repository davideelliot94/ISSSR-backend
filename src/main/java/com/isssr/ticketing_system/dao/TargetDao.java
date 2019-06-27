package com.isssr.ticketing_system.dao;

import com.isssr.ticketing_system.entity.ScrumProductWorkflow;
import com.isssr.ticketing_system.entity.ScrumTeam;
import com.isssr.ticketing_system.entity.Target;
import com.isssr.ticketing_system.enumeration.TargetState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TargetDao extends JpaRepository<Target, Long >  {
    Target findByName(String name);

    boolean existsByName(String name);

    @Query("select t from Target t where t.targetState = :state")
    List<Target> getActiveTarget(@Param("state") TargetState targetState);

    @Query("select t from Target t JOIN t.scrumTeam s where s.productOwner.id = :id ")
    List<Target> findByProductOwnerId(@Param("id") Long productOwnerId);

    List<Target> findAllByScrumTeamIn(List<ScrumTeam> scrumTeam);

    List<Target> findAllByScrumProductWorkflow(ScrumProductWorkflow scrumProductWorkflow);

    List<Target> findAllByScrumProductWorkflowIsNotNullAndScrumTeamIsNotNull();
}
