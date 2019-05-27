package com.isssr.ticketing_system.enumeration;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Area di problemi risolti da un Team.
 *
 */
@JsonFormat(shape = JsonFormat.Shape.STRING)
@SuppressWarnings("unused")
public enum ProblemArea {

    Service_Payment,
    Service_Technical,
    Product_Technical,
    Product_Payment
}
