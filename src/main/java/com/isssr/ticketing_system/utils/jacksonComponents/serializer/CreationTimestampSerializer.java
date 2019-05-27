package com.isssr.ticketing_system.utils.jacksonComponents.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.isssr.ticketing_system.controller.TargetController;
import com.isssr.ticketing_system.entity.Target;
import com.isssr.ticketing_system.exception.EntityNotFoundException;
import com.isssr.ticketing_system.utils.SpringRootContext;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

public class CreationTimestampSerializer extends StdSerializer<Instant> {

    public CreationTimestampSerializer() {
        this(null);
    }

    public CreationTimestampSerializer(Class t) {
        super(t);
    }

    @Override
    public void serialize(Instant instant, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        Date date = Date.from(instant);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String formattedDate = formatter.format(date);

        jsonGenerator.writeString(formattedDate);
    }

}
