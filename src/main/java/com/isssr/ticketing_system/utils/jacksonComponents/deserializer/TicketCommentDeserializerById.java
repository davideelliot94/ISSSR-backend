package com.isssr.ticketing_system.utils.jacksonComponents.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.isssr.ticketing_system.controller.TicketCommentController;
import com.isssr.ticketing_system.entity.TicketComment;
import com.isssr.ticketing_system.utils.SpringRootContext;
//import com.sun.javafx.animation.TickCalculation;

import java.io.IOException;
import java.util.Optional;

public class TicketCommentDeserializerById extends StdDeserializer<TicketComment> {

    private final TicketCommentController ticketCommentController;

    public TicketCommentDeserializerById() {
        this(null);
    }

    public TicketCommentDeserializerById(Class<?> clazz) {
        super(clazz);
        ticketCommentController = SpringRootContext.getApplicationContext().getBean(TicketCommentController.class);
    }

    @Override
    public TicketComment deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        Long id = node.asLong();
        Optional<TicketComment> comment = ticketCommentController.findById(id);
        return comment.isPresent() ? comment.get() : null;
    }

}
