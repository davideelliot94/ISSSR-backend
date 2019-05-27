package com.isssr.ticketing_system.controller;

import com.isssr.ticketing_system.exception.DomainEntityNotFoundException;
import com.isssr.ticketing_system.logger.aspect.LogOperation;
import com.isssr.ticketing_system.entity.ACLRecord;
import com.isssr.ticketing_system.dao.ACLRecordDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.List;

@Service
@Qualifier(value = "ACLRecordController")
public class ACLRecordController {


    private final ACLRecordDAO aclRecordDAO;

    @LogOperation(inputArgs = {"ID"})
    protected boolean verifyIfExists(Long ID) {
        boolean exists = aclRecordDAO.existsById(ID);
        if (!exists) {
            throw new DomainEntityNotFoundException(ID, ACLRecord.class);
        }
        return true;
    }

    //@Async
    //@Transactional(noRollbackFor = Exception.class)
    public ACLRecord saveRecord(ACLRecord record) {
        aclRecordDAO.saveAndFlush(record);
         return record;
    }

    @Autowired
    public ACLRecordController(ACLRecordDAO dao) {
        this.aclRecordDAO = dao;
    }

    public List<ACLRecord> getAllACLRecord() {
        return aclRecordDAO.findAll();
    }

    public ACLRecord getACLRecord(@NotNull Long ID) {
        verifyIfExists(ID);
        return aclRecordDAO.getOne(ID);
    }

    public ACLRecord getACLRecordBySid(@NotNull String sid) {
        return aclRecordDAO.findBySid(sid);
    }

    @Transactional
    public void removeACLRecordById(@NotNull Long id) {
        aclRecordDAO.deleteById(id);
    }

    @Transactional
    public void removeACLRecords() {
        aclRecordDAO.deleteAll();
    }

}


