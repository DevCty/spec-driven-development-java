package com.stavaray.auth.infrastructure.config;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;

@Configuration
public class LoggingConfig {

    public LoggingConfig() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        context.reset();

        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("logback-spring.xml")) {
            if (stream != null) {
                configurator.doConfigure(stream);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Error configuring Logback", e);
        }
    }
}
