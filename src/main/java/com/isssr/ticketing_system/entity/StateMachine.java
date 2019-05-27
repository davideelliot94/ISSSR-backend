package com.isssr.ticketing_system.entity;

import javax.persistence.*;

/**
 * Rappresenta una macchina a stati di Default per il workflow di un ticket
 *
 *
 */
@Entity
@SuppressWarnings("unused")
public class StateMachine {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String name;

    @Transient
    private String base64StateMachine;

    @Lob
    private String base64Diagram;

    public StateMachine(){

    }

    public String getBase64Diagram() {
        return base64Diagram;
    }

    public void setBase64Diagram(String base64Diagram) {
        this.base64Diagram = base64Diagram;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBase64StateMachine() {
        return base64StateMachine;
    }

    public void setBase64StateMachine(String base64StateMachine) {
        this.base64StateMachine = base64StateMachine;
    }
}
