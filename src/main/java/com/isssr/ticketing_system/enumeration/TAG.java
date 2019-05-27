package com.isssr.ticketing_system.enumeration;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Il TAG e' una parola chiave che un'utente puo' indicare all'apertura di un Ticket,
 * Usato per il filtraggio e la caratterizzazione dei Ticket.
 *
 */
@JsonFormat(shape = JsonFormat.Shape.STRING)
@SuppressWarnings("unused")
public enum TAG {
    Java,
    Python,
    C,
    PHP,
    Asp,
    Ruby,
    Javascript,
    Hibernate,
    Spring,
    Laravel,
    Jpa,
    Jdbc,
    Rest,
    API,
    Json,
    Query,
    Syntax ,
    Transaction,
    Deployment,
    GUI,
    Relations,
    Rollback,
    Dump,
    Injection,
    Performance,
    Server,
    Cost,
    Billing,
    Port,
    Connection,
    Settings,
    CORS,
    Export,
    Installation


}
