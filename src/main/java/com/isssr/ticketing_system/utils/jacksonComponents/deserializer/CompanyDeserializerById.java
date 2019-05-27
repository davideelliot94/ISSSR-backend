package com.isssr.ticketing_system.utils.jacksonComponents.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.isssr.ticketing_system.controller.CompanyController;
import com.isssr.ticketing_system.entity.Company;
import com.isssr.ticketing_system.utils.SpringRootContext;

import java.io.IOException;
import java.util.Optional;

public class CompanyDeserializerById extends StdDeserializer<Company> {

    private final CompanyController companyController;

    public CompanyDeserializerById() {
        this(null);
    }

    public CompanyDeserializerById(Class<?> clazz) {
        super(clazz);
        companyController = SpringRootContext.getApplicationContext().getBean(CompanyController.class);
    }

    @Override
    public Company deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        Long id = node.asLong();

        Optional<Company> company = companyController.findById(id);
        return company.isPresent() ? company.get() : null;
    }

}
