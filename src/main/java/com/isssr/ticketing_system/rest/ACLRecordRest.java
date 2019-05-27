package com.isssr.ticketing_system.rest;


import com.isssr.ticketing_system.entity.ACLRecord;
import com.isssr.ticketing_system.controller.ACLRecordController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "acl-records")
public class ACLRecordRest {

    private final ACLRecordController aclRecordController;

    @Autowired
    public ACLRecordRest(ACLRecordController aclRecordController) {
        this.aclRecordController = aclRecordController;
    }

    @GetMapping("{id}")
    @ResponseStatus(OK)
    ACLRecord getACLRecordBySID(@PathVariable Long id) {
        return aclRecordController.getACLRecord(id);
    }

    @GetMapping("{SID}")
    @ResponseStatus(OK)
    ACLRecord getACLRecordBySID(@PathVariable String SID) {
        return aclRecordController.getACLRecordBySid(SID);
    }

    @GetMapping
    @ResponseStatus(OK)
    List<ACLRecord> getAllACLRecord() {
        return aclRecordController.getAllACLRecord();
    }

    @DeleteMapping("{id}")
    @ResponseStatus(NO_CONTENT)
    public void deleteACLRecordById(@PathVariable Long id) {
        aclRecordController.removeACLRecordById(id);
    }

    /*
    @RequestMapping(path = "{SID}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteACLRecordBySid(@PathVariable String SID) {
        aclRecordController.removeACLRecordById(
                aclRecordController.getACLRecordBySid(SID).getId()
        );
    }
    */

    @DeleteMapping
    @ResponseStatus(NO_CONTENT)
    public void deleteRequestsLogs() {
        aclRecordController.removeACLRecords();
    }

}

