package com.isssr.ticketing_system.utils.jacksonComponents.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.isssr.ticketing_system.controller.TicketRelationController;
import com.isssr.ticketing_system.entity.TicketRelation;
import com.isssr.ticketing_system.utils.SpringRootContext;

import java.io.IOException;
import java.util.Optional;

public class TicketRelationDeserializerById extends StdDeserializer<TicketRelation> {

    private final TicketRelationController ticketRelationController;

    public TicketRelationDeserializerById() {
        this(null);
    }

    public TicketRelationDeserializerById(Class<?> clazz) {
        super(clazz);
        ticketRelationController = SpringRootContext.getApplicationContext().getBean(TicketRelationController.class);
    }

    @Override
    public TicketRelation deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        Long id = node.asLong();
        Optional<TicketRelation> relation = ticketRelationController.findById(id);
        return relation.isPresent() ? relation.get() : null;
    }

}
