package com.isssr.ticketing_system.dto;

import lombok.Data;

/* Viene usata per restituire la lista di prodotti di cui un utente può visualizzare lo storico
 * i dettagli degli Sprint. Is a kind of SprintDTO, possiede in aggiunta due booleani che sono settati
 * in base al ruolo dell'utente*/
@Data
public class TargetWithUserRoleDto extends TargetDto {
    private Boolean isUserScrumMaster;
    private Boolean isUserProductOwner;
}
