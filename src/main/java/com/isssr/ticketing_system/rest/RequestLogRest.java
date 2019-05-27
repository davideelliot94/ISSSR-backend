package com.isssr.ticketing_system.rest;

import com.isssr.ticketing_system.entity.RequestLog;
import com.isssr.ticketing_system.controller.RequestLoggerController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "requests-logs")
public class RequestLogRest {

    private final RequestLoggerController requestLoggerController;

    @Autowired
    public RequestLogRest(RequestLoggerController requestLoggerController) {
        this.requestLoggerController = requestLoggerController;
    }

    @GetMapping("{id}")
    @ResponseStatus(OK)
    RequestLog getRequestLog(@PathVariable Long id) {
        return requestLoggerController.getRequestLog(id);
    }

    @GetMapping
    @ResponseStatus(OK)
    List<RequestLog> getRequestLogs() {
        return requestLoggerController.getRequestsLogs();
    }

    @DeleteMapping("{id}")
    @ResponseStatus(NO_CONTENT)
    public void deleteRequestLog(@PathVariable Long id) {
        requestLoggerController.removeRequestLog(id);
    }

    @DeleteMapping
    @ResponseStatus(NO_CONTENT)
    public void deleteRequestsLogs() {
        requestLoggerController.removeRequestsLogs();
    }

}
