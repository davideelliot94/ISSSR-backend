package com.isssr.ticketing_system.dto;

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.List;

/* Un ScrumProductWorkflowDto è un oggetto Bean che incapsula gli attributi di interesse per l'oggetto
 * ScrumProductWorkflow. E' l'oggetto restituito e ricevuto dall'interfaccia REST del sistema */

@Data
public class ScrumProductWorkflowDto {

    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private List<String> states;

}

