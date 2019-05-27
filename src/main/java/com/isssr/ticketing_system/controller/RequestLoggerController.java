package com.isssr.ticketing_system.controller;

import com.isssr.ticketing_system.exception.DomainEntityNotFoundException;
import com.isssr.ticketing_system.entity.RequestLog;
import com.isssr.ticketing_system.dao.LogDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class RequestLoggerController {

    private final static String CLASS_NAME = RequestLoggerController.class.getSimpleName();
    private final static Logger LOGGER = Logger.getLogger(CLASS_NAME);

    private final LogDAO logDAO;

    @Autowired
    public RequestLoggerController(LogDAO logDAO) {
        this.logDAO = logDAO;
    }

    //@Async
    @Transactional
    public void saveRequestLog(RequestLog requestLog) {
        logDAO.save(requestLog);
    }

    public RequestLog getRequestLog(@NotNull Long id) {
        Optional<RequestLog> log = logDAO.findById(id);
        if (!log.isPresent()) {
            throw new DomainEntityNotFoundException(id, RequestLog.class);
        }
        return log.get();
    }

    public List<RequestLog> getRequestsLogs() {
        return logDAO.findAll();
    }

    @Transactional
    public void removeRequestLog(@NotNull Long id) {
        logDAO.delete(getRequestLog(id));
    }

    @Transactional
    public void removeRequestsLogs() {
        logDAO.deleteAll();
    }

    public Logger getLogger() {
        return LOGGER;
    }

}
