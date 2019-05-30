package com.isssr.ticketing_system.enumeration;

import com.fasterxml.jackson.annotation.JsonFormat;

/* Gli stati che possono essere assunti da un item dello scrum backlog.*/
@JsonFormat(shape = JsonFormat.Shape.STRING)
@SuppressWarnings("unused")
public enum BacklogItemStatus {
    INIT, // Non nello sprint backlog
    TODO, // Nello sprint backlog ma non ancora avviato
    EXECUTION, // Nello sprint backlog e in esecuzione
    COMPLETED  // Completato
}