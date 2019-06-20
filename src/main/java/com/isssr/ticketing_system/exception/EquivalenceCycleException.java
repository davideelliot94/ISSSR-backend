package com.isssr.ticketing_system.exception;

/* Questa eccezione viene sollevata quando nel tentativo di creare una relazione di equivalenza tra due ticket, si
* introduce un ciclo di dipendenze.*/
public class EquivalenceCycleException extends Exception {
}
