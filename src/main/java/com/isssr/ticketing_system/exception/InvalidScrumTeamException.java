package com.isssr.ticketing_system.exception;

/* Sollevata quando si prova a creare uno Scrum Team con utenti inesistenti o che sono di tipo CUSTOMER*/
public class InvalidScrumTeamException extends Exception {
    public InvalidScrumTeamException() {
    }

    public InvalidScrumTeamException(String message) {
        super(message);
    }
}
