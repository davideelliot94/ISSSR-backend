package com.isssr.ticketing_system.enumeration;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Difficolta' di risoluzione di un Ticket cosi' come indicata dal Team Coordinator
 *
 */
@JsonFormat(shape = JsonFormat.Shape.STRING)
@SuppressWarnings("unused")
public enum TicketDifficulty {

    LOW,
    MEDIUM,
    HIGH
}
