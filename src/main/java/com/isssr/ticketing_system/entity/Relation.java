package com.isssr.ticketing_system.entity;

import com.isssr.ticketing_system.logger.aspect.LogClass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "relation")
@NoArgsConstructor
@Getter
@Setter
@LogClass(idAttrs = {"name"})
public class Relation {

    @Id
    @Column(name = "name")
    private String name;
    private Boolean cyclic;

    public Relation(String name, Boolean cyclic){
        this.name = name;
        this.cyclic = cyclic;
    }

}
