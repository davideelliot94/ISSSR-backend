package com.isssr.ticketing_system.controller;

import com.isssr.ticketing_system.dao.RelationDao;
import com.isssr.ticketing_system.entity.Relation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.util.List;

@Service
public class RelationController {

    @Autowired
    private RelationDao relationDao;

    @Transactional
    public @NotNull Relation createRelation(@NotNull Relation relation, @NotNull String name) {
        Relation newRelation =null;
        if(!relationDao.existsById(name))
            newRelation = relationDao.save(relation);
        return newRelation;
    }

    @Transactional
    public Relation findRelationById(@NotNull String name) {
        Relation relation = relationDao.getOne(name);
        return relation;
    }

    @Transactional
    public List<Relation> findAllRelations() {
        List<Relation> relations = relationDao.findAll();
        return relations;
    }
}
