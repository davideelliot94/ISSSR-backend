package com.isssr.ticketing_system.dto;

import lombok.Data;

/* Oggetto Bean utilizzato dallo strato di boundary dell'applicazione in luogo dell'entity User corrispondente*/
@Data
public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
}
