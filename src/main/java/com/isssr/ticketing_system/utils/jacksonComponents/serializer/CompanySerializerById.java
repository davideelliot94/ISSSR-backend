package com.isssr.ticketing_system.utils.jacksonComponents.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.isssr.ticketing_system.entity.Company;
//import com.isssr.ticketing_system.entity.Role;

import java.io.IOException;

public class CompanySerializerById extends StdSerializer<Company> {

    public CompanySerializerById() {
        this(null);
    }

    public CompanySerializerById(Class t) {
        super(t);
    }

    @Override
    public void serialize(Company company, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeNumber(company.getId());
    }

}
