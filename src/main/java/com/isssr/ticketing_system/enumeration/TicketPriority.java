package com.isssr.ticketing_system.enumeration;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Priorita' assegnata ad un Ticket da Customer e/o Personale Interno.
 *
 */
@JsonFormat(shape = JsonFormat.Shape.STRING)
@SuppressWarnings("unused")
public enum TicketPriority {
    LOW,    //0
    MEDIUM, //1
    HIGH    //2
}
