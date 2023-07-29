package com.senderman.jlogrep.controller;

import com.senderman.jlogrep.config.ApplicationProperties;
import com.senderman.jlogrep.config.ConfigMapper;
import com.senderman.jlogrep.exception.InvalidConfigFormatException;
import com.senderman.jlogrep.model.response.Message;
import com.senderman.jlogrep.model.rule.ConfigRule;
import com.senderman.jlogrep.model.rule.RuleFilter;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.http.server.types.files.StreamedFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Named;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
public class UtilsController {

    private final ApplicationProperties app;
    private final List<String> internalTags;
    private final ConfigMapper configMapper;
    private final PrometheusMeterRegistry meterRegistry;

    public UtilsController(
            ApplicationProperties app,
            @Named("internalTags") List<String> internalTags,
            ConfigMapper configMapper,
            PrometheusMeterRegistry meterRegistry
    ) {
        this.app = app;
        this.internalTags = internalTags.stream().sorted().toList();
        this.configMapper = configMapper;
        this.meterRegistry = meterRegistry;
    }

    @Post("/rules")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(
            summary = "Get tags by rules",
            description = "Parse internal or supplied rules and return list of tags"
    )
    @ApiResponse(responseCode = "200")
    @ApiResponse(
            responseCode = "400",
            description = "Invalid config format",
            content = @Content(schema = @Schema(implementation = Message.class))
    )
    public List<String> rules(
            @Parameter(description = "rules.yml to parse. Use internal if not supplied")
            @Nullable CompletedFileUpload rules
    )
            throws IOException, InvalidConfigFormatException {

        if (rules == null)
            return internalTags;

        List<ConfigRule> uploadedRules = configMapper.mapRules(rules.getInputStream());

        return listTags(uploadedRules);
    }

    @Operation(hidden = true)
    @Get("/metrics")
    @Produces(MediaType.TEXT_PLAIN)
    public String metrics() {
        return meterRegistry.scrape();
    }

    @Get("/filters")
    @Operation(
            summary = "Get filters",
            description = "Get supported filters"
    )
    public RuleFilter[] filters() {
        return RuleFilter.values();
    }

    @Get("/appinfo")
    @Operation(
            summary = "Info about app",
            description = "Get info about app name, version and authors"
    )
    public ApplicationProperties info() {
        return app;
    }

    @Operation(hidden = true)
    @Get("/rules-download")
    @Header(name = "Content-Disposition", value = "attachment")
    public StreamedFile rulesDownload() {
        return new StreamedFile(getClass().getResourceAsStream("/rules.yml"), MediaType.APPLICATION_YAML_TYPE)
                .attach("rules.yml");
    }

    @Operation(hidden = true)
    @Get("/dateformat-download")
    public StreamedFile dateFormatDownload() {
        return new StreamedFile(getClass().getResourceAsStream("/dateformat.yml"), MediaType.APPLICATION_YAML_TYPE)
                .attach("dateformat.yml");
    }

    @Operation(hidden = true)
    @Get("/jar")
    public StreamedFile jar() throws IOException {
        // pipe zip output stream to streamed file which will be delivered to the client
        var pipedOut = new PipedOutputStream();
        var pipedIn = new PipedInputStream(pipedOut);
        new Thread(() -> {
            try (var zipOut = new ZipOutputStream(pipedOut)) {
                // create new zip entry for jar file
                var entry = new ZipEntry("%s-%s.jar".formatted(app.getName(), app.getVersion()));
                zipOut.putNextEntry(entry);
                // read the whole jar file. WON'T WORK IF RUN USING GRADLE / IDEA!!!
                var bytes = Files.readAllBytes(Path.of(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()));
                // compress the jar file and put it into zip
                zipOut.write(bytes);
                zipOut.closeEntry();
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }).start();

        // deliver zip archive with jar file to the client while compressing
        return new StreamedFile(
                pipedIn,
                new MediaType("application/zip"))
                .attach("%s-%s.zip".formatted(app.getName(), app.getVersion()));
    }

    private List<String> listTags(List<ConfigRule> rules) {
        return rules
                .stream()
                .map(ConfigRule::getTag)
                .distinct()
                .sorted()
                .toList();
    }

}
