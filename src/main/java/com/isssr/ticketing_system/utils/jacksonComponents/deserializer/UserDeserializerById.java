package com.isssr.ticketing_system.utils.jacksonComponents.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.isssr.ticketing_system.controller.UserController;
import com.isssr.ticketing_system.entity.User;
import com.isssr.ticketing_system.exception.EntityNotFoundException;
import com.isssr.ticketing_system.utils.SpringRootContext;

import java.io.IOException;
import java.util.Optional;

public class UserDeserializerById extends StdDeserializer<User> {

    private final UserController userController;

    public UserDeserializerById() {
        this(null);
    }

    public UserDeserializerById(Class<?> clazz) {
        super(clazz);
        userController = SpringRootContext.getApplicationContext().getBean(UserController.class);
    }

    @Override
    public User deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        Long id = node.asLong();
        User user = null;
        try {
            user = userController.findById(id);
        } catch (EntityNotFoundException e) {
            return null;
        }
        return user;
    }

}
