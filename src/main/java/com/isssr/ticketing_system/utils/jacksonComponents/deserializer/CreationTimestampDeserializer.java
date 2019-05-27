package com.isssr.ticketing_system.utils.jacksonComponents.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.isssr.ticketing_system.controller.TargetController;
import com.isssr.ticketing_system.entity.Target;
import com.isssr.ticketing_system.exception.EntityNotFoundException;
import com.isssr.ticketing_system.utils.SpringRootContext;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class CreationTimestampDeserializer extends StdDeserializer<Instant> {

    public CreationTimestampDeserializer() {
        this(null);
    }

    public CreationTimestampDeserializer(Class<?> clazz) {
        super(clazz);
    }

    @Override
    public Instant deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        String timestamp = node.asText();
        return LocalDateTime.parse(
                timestamp ,
                DateTimeFormatter.ofPattern( "dd/MM/yyyy HH:mm:ss" , Locale.US )
        )
                .atZone(
                        ZoneId.of( "America/Toronto" )
                )
                .toInstant();
    }

}
