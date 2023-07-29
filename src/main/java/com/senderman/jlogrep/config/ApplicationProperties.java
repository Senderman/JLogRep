package com.senderman.jlogrep.config;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

@ConfigurationProperties("micronaut.application")
@Serdeable
@Schema(description = "Info about application")
public interface ApplicationProperties {

    @Schema(description = "Name of the program")
    String getName();

    @Schema(description = "Version of the program")
    String getVersion();

    @Schema(description = "Authors of the program")
    String getAuthors();

}
