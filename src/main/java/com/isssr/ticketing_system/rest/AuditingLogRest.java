package com.isssr.ticketing_system.rest;

import com.isssr.ticketing_system.logger.RecordController;
import com.isssr.ticketing_system.logger.entity.Record;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;


@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "auditing-logs", produces = "application/json")
public class AuditingLogRest {

    private final RecordController recordController;

    public AuditingLogRest(RecordController recordController) {
        this.recordController = recordController;
    }

    @GetMapping
    @ResponseStatus(OK)
    public List<Record> getAllRecords() {
        return recordController.getAllRecords();
    }

    @GetMapping("{id}")
    @ResponseStatus(OK)
    Record getRecord(@PathVariable Long id) {
        return recordController.getRecordById(id.intValue());
    }

    @DeleteMapping("{id}")
    @ResponseStatus(NO_CONTENT)
    public boolean deleteRecord(@PathVariable Long id) {
        return recordController.deleteRecord(id.intValue());
    }

    @DeleteMapping
    @ResponseStatus(NO_CONTENT)
    public void deleteAllRecords() {
        List<Record> records = recordController.getAllRecords();
        if (records != null) records.forEach(r -> deleteRecord(r.getId().longValue()));
    }

}
