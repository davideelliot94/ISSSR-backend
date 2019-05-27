package com.isssr.ticketing_system.enumeration;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Enumerazione che rappresenta il tipo di un Target.
 *
 */
@JsonFormat(shape = JsonFormat.Shape.STRING)
@SuppressWarnings("unused")
public enum TargetType {

    Product,
    Service
}
