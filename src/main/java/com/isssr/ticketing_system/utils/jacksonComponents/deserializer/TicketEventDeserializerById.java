package com.isssr.ticketing_system.utils.jacksonComponents.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.isssr.ticketing_system.controller.TicketEventController;
import com.isssr.ticketing_system.entity.TicketEvent;
import com.isssr.ticketing_system.utils.SpringRootContext;

import java.io.IOException;
import java.util.Optional;

public class TicketEventDeserializerById extends StdDeserializer<TicketEvent> {

    private final TicketEventController ticketEventController;

    public TicketEventDeserializerById() {
        this(null);
    }

    public TicketEventDeserializerById(Class<?> clazz) {
        super(clazz);
        ticketEventController = SpringRootContext.getApplicationContext().getBean(TicketEventController.class);
    }

    @Override
    public TicketEvent deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        Long id = node.asLong();
        Optional<TicketEvent> event = ticketEventController.findById(id);
        return event.isPresent() ? event.get() : null;
    }

}
