package com.isssr.ticketing_system.enumeration;

import com.fasterxml.jackson.annotation.JsonFormat;


@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum TicketStatus {

    VALIDATION(2),
    DISPATCHING(2),
    EDIT(3),
    PENDING(1),
    EXECUTION(5),
    ACCEPTANCE(15),
    REOPENED(4),
    CLOSED(0);

    //Misurato in Giorni.
    private int TTL;

    TicketStatus(int TTL){
        this.TTL = TTL;
    }

    public int getTTL() {
        return TTL;
    }

    public static TicketStatus getEnum(String stateStr){

        for(TicketStatus ticketStatus : TicketStatus.values()){
            if(stateStr.equals(ticketStatus.toString()))
                return ticketStatus;
        }
        return null;


    }


    public static boolean validateState(String stateStr){

        for(TicketStatus ticketStatus : TicketStatus.values()){
            if(stateStr.equals(ticketStatus.toString()))
                return true;
        }
        return false;

    }
}
