/*package com.isssr.ticketing_system.utils.jacksonComponents.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.isssr.ticketing_system.controller.TicketAttachmentController;
import com.isssr.ticketing_system.entity.TicketAttachment;
import com.isssr.ticketing_system.utils.SpringRootContext;

import java.io.IOException;
import java.util.Optional;

public class TicketAttachmentDeserializerById extends StdDeserializer<TicketAttachment> {

    private final TicketAttachmentController ticketAttachmentController;

    public TicketAttachmentDeserializerById() {
        this(null);
    }

    public TicketAttachmentDeserializerById(Class<?> clazz) {
        super(clazz);
        ticketAttachmentController = SpringRootContext.getApplicationContext().getBean(TicketAttachmentController.class);
    }

    @Override
    public TicketAttachment deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        Long id = node.asLong();
        Optional<TicketAttachment> attachment = ticketAttachmentController.findById(id);
        return attachment.isPresent() ? attachment.get() : null;
    }

}
*/