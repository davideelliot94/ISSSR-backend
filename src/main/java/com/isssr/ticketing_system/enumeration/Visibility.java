package com.isssr.ticketing_system.enumeration;

import com.fasterxml.jackson.annotation.JsonFormat;


/**
 * Visibilita' di un Ticket ai Customer diversi dall'apertore.
 *
 */
@JsonFormat(shape = JsonFormat.Shape.STRING)
@SuppressWarnings("unused")
public enum Visibility {

    PUBLIC,
    PRIVATE
}
