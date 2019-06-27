package com.isssr.ticketing_system.dto;

        import lombok.Data;

/* Viene usata per restituire la lista di sprint di cui un utente può visualizzare lo storico
 * delle scrum ceremonies. Is a kind of SprintDTO, possiede in aggiunta un booleano che è settato
 * a true se l'utente è scrum master, false altrimenti*/
@Data
public class SprintWithUserRoleDto extends SprintDTO {

    private Boolean isUserScrumMaster;
    private Boolean isUserProductOwner;

}
