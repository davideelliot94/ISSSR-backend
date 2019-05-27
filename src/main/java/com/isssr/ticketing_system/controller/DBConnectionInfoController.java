package com.isssr.ticketing_system.controller;

import com.isssr.ticketing_system.entity.db_connection.DBConnectionInfo;
import com.isssr.ticketing_system.dao.DBConnectionInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DBConnectionInfoController {

    @Autowired
    private DBConnectionInfoRepository dbConnectionInfoRepository;

    @Transactional
    public Iterable<DBConnectionInfo> findAll() {
        return this.dbConnectionInfoRepository.findAll();
    }

    @Transactional
    public DBConnectionInfo findByUrlAndUsernameAndPassword(String url, String username, String password) {

        //search it
        DBConnectionInfo dbConnectionInfo = this.dbConnectionInfoRepository.findByUrlAndUsernameAndPassword(
                url,
                username,
                password
        );

        //check if is already in db
        if (dbConnectionInfo == null) {

            dbConnectionInfo = new DBConnectionInfo(url, username, password);

            //create new connection info
            dbConnectionInfo = this.dbConnectionInfoRepository.save(dbConnectionInfo);

        }

        //return db connection info
        return dbConnectionInfo;

    }
}
