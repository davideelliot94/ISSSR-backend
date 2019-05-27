package com.isssr.ticketing_system.configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class JacksonConfigurator {

    @Bean
    public ObjectMapper getObjectMapper() {
        return new ObjectMapper().addMixIn(Object.class, IgnoreHibernatePropertiesInJackson.class);
    }

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private abstract class IgnoreHibernatePropertiesInJackson {

    }

}