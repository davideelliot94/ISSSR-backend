package com.isssr.ticketing_system.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


@Getter
@Setter
@Table(name = "acl_record")
@Entity
public class ACLRecord implements Serializable {

    @Column(name = "record_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sid")
    private String sid;

    @Column(name = "timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss", timezone = "Europe/Rome")
    private Date timestamp;

    @Column(name = "type")
    private String type;

    @Column(name = "type_id")
    private long type_id;

    @Column(name = "permission_mask")
    private int permission;

    @Column(name = "is_successful")
    private boolean isGranting;


    public ACLRecord(String sid, String type, long type_id, int permission, boolean isGranting) {
        this.sid = sid;
        this.type = type;
        this.type_id = type_id;
        this.permission = permission;
        this.isGranting = isGranting;
        this.timestamp = new Date();
    }

    public ACLRecord() {
        this.timestamp = new Date();
    }
}
