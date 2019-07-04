package com.isssr.ticketing_system.dto;

import com.isssr.ticketing_system.enumeration.BacklogItemStatus;
import com.isssr.ticketing_system.enumeration.TicketPriority;
import lombok.Data;
import java.sql.Date;
import java.time.LocalDate;

/*Un BacklogItemDto è un oggetto Bean che incapsula gli attributi di interesse per l'oggetto BacklogItem.
 * E' l'oggetto restituito e ricevuto dall'interfaccia REST del sistema quando si gestiscono gli item del backlog.*/
@Data
public class BacklogItemDto {

    private Long id;
    private String title;
    private String description;
    private Integer priority;
    private String status;
    private Integer effortEstimation;
    private LocalDate finishDate;

}