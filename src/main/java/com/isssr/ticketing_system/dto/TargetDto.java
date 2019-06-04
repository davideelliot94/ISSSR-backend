package com.isssr.ticketing_system.dto;

import lombok.Data;

/*Un TargetDto è un oggetto Bean che incapsula gli attributi di interesse per l'oggetto Target.
 * E' l'oggetto restituito e ricevuto dall'interfaccia REST del sistema quando si gestiscono prodotti.*/

@Data
public class TargetDto {
    private Long id;
    private String name;
}
