package com.isssr.ticketing_system.entity;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.isssr.ticketing_system.entity.SoftDelete.SoftDeletableEntity;
import com.isssr.ticketing_system.enumeration.Visibility;
import com.isssr.ticketing_system.response_entity.JsonViews;
import com.isssr.ticketing_system.utils.jacksonComponents.deserializer.InstantDeserializer;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.time.Instant;

//@Data
@NoArgsConstructor
@RequiredArgsConstructor

@Getter
@Setter

@Entity
@DynamicInsert
@DynamicUpdate
public class TicketComment extends SoftDeletableEntity {
    @JsonView(JsonViews.IdentifierOnly.class)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
/*
    @JsonView(JsonViews.Basic.class)
    @NonNull
    private String content;
*/
    @JsonView(JsonViews.Basic.class)
    @NonNull
    //@JsonDeserialize(using = InstantDeserializer.class)
    private String timestamp;

    @JsonView(JsonViews.Basic.class)
    @NonNull
    private Visibility visibility;
/*
    @JsonView(JsonViews.Basic.class)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;
*/

    @ManyToOne
    private User eventGenerator;

    private String description;
}
