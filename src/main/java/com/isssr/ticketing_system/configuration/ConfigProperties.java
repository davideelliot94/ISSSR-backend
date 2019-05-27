package com.isssr.ticketing_system.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;


@Component
@ConfigurationProperties
@PropertySource("classpath:baseConfiguration.properties")
@Getter @Setter
@ToString
public class ConfigProperties {

    @Getter @Setter
    @ToString
    public static class DebugConfig {

        private boolean debug;

    }

    private DebugConfig debugConfig = new DebugConfig();
    private String attachementsStorage = System.getProperty("java.io.tmpdir", "/tmp");

}
