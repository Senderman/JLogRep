package com.senderman.jlogrep.config;

import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;

@Singleton
public class ApplicationProperties {
    private final String name;
    private final String version;
    private final String authors;

    public ApplicationProperties(
            @Value("${micronaut.application.name}") String name,
            @Value("${app.version}") String version,
            @Value("${app.authors}") String authors) {
        this.name = name;
        this.version = version;
        this.authors = authors;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getAuthors() {
        return authors;
    }
}
