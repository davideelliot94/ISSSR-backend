package com.isssr.ticketing_system.utils.jacksonComponents.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.isssr.ticketing_system.controller.TargetController;
import com.isssr.ticketing_system.entity.Target;
import com.isssr.ticketing_system.exception.EntityNotFoundException;
import com.isssr.ticketing_system.exception.NotFoundEntityException;
import com.isssr.ticketing_system.utils.SpringRootContext;

import java.io.IOException;

public class TargetDeserializerById extends StdDeserializer<Target> {

    private final TargetController targetController;

    public TargetDeserializerById() {
        this(null);
    }

    public TargetDeserializerById(Class<?> clazz) {
        super(clazz);
        targetController = SpringRootContext.getApplicationContext().getBean(TargetController.class);
    }

    @Override
    public Target deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        Long id = node.asLong();
//        if (id == 0) return null;
        try {
            return targetController.getTargetById(id);
        } catch (NotFoundEntityException e) {
            e.printStackTrace();
        }
        return null;
    }

}
