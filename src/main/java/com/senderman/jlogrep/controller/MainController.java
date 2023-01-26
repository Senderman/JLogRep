package com.senderman.jlogrep.controller;

import com.senderman.jlogrep.archive.Archive;
import com.senderman.jlogrep.archive.ArchiveDetector;
import com.senderman.jlogrep.config.ApplicationProperties;
import com.senderman.jlogrep.config.ConfigMapper;
import com.senderman.jlogrep.container.ArchiveLogsContainer;
import com.senderman.jlogrep.container.LogsContainer;
import com.senderman.jlogrep.container.PlainTextLogsContainer;
import com.senderman.jlogrep.exception.BadRequestException;
import com.senderman.jlogrep.exception.EmptyBugreportName;
import com.senderman.jlogrep.exception.InvalidConfigFormatException;
import com.senderman.jlogrep.model.request.RegexRequest;
import com.senderman.jlogrep.model.request.ScanRequest;
import com.senderman.jlogrep.model.response.Message;
import com.senderman.jlogrep.model.response.Problem;
import com.senderman.jlogrep.model.rules.FileRule;
import com.senderman.jlogrep.model.rules.RuleFilter;
import com.senderman.jlogrep.model.rules.RuleType;
import com.senderman.jlogrep.model.rules.YamlRule;
import com.senderman.jlogrep.scanner.LogScanner;
import com.senderman.jlogrep.util.DefaultScanOptionsSupplier;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.*;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.views.View;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class MainController {

    private final LogScanner scanner;
    private final DefaultScanOptionsSupplier scanOptionsSupplier;
    private final ConfigMapper configMapper;
    private final List<YamlRule> internalRules;
    private final ApplicationProperties app;

    public MainController(
            LogScanner scanner,
            DefaultScanOptionsSupplier scanOptionsSupplier,
            ConfigMapper configMapper,
            List<YamlRule> internalRules,
            ApplicationProperties app
    ) {
        this.scanner = scanner;
        this.scanOptionsSupplier = scanOptionsSupplier;
        this.configMapper = configMapper;
        this.internalRules = internalRules;
        this.app = app;
    }

    @Post("/scan")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Collection<Problem> scan(
            CompletedFileUpload bugreport,
            @Nullable CompletedFileUpload rules,
            @Nullable CompletedFileUpload dateFormat,
            ScanRequest r
    ) throws IOException {

        var bugreportName = bugreport.getFilename();
        if (bugreportName == null)
            throw new EmptyBugreportName();

        var bugreportIn = copyToMemoryAndClose(bugreport.getInputStream());
        var archiveType = ArchiveDetector.detectArchiveType(bugreportName);
        LogsContainer logsContainerToScan;

        if (archiveType == null) {
            logsContainerToScan = new PlainTextLogsContainer(bugreportIn, bugreportName);
        } else {
            Archive bugreportArchive = ArchiveDetector.createArchive(bugreportIn, archiveType);
            logsContainerToScan = new ArchiveLogsContainer(bugreportArchive);
        }

        var scanOptions = scanOptionsSupplier.get();

        if (rules != null)
            try (var rulesStream = rules.getInputStream()) {
                scanOptions.setYamlRules(configMapper.mapRules(rulesStream));
            }

        if (dateFormat != null)
            try (var dateFormatSteam = dateFormat.getInputStream()) {
                scanOptions.setDateFormat(configMapper.mapLogDateFormat(dateFormatSteam));
            }

        if (r.getDate() != null)
            scanOptions.setProblemDate(r.getDate());

        if (r.getTags() != null)
            scanOptions.setTags(r.getTags());
        if (r.getFilters() != null)
            scanOptions.setFilters(r.getFilters());

        scanOptions.setYear(r.getYear());
        scanOptions.setInterval(r.getInterval());
        scanOptions.setShow(r.getShow());

        return scanner.scan(logsContainerToScan, scanOptions);
    }

    @Post("/regex")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Collection<Problem> regex(
            CompletedFileUpload bugreport,
            @Nullable CompletedFileUpload dateFormat,
            RegexRequest r) throws IOException {

        var bugreportName = bugreport.getFilename();
        if (bugreportName == null)
            throw new EmptyBugreportName();

        var bugreportIn = copyToMemoryAndClose(bugreport.getInputStream());
        var archiveType = ArchiveDetector.detectArchiveType(bugreportName);
        LogsContainer logsContainerToScan;

        if (archiveType == null) {
            logsContainerToScan = new PlainTextLogsContainer(bugreportIn, bugreportName);
        } else {
            Archive bugreportArchive = ArchiveDetector.createArchive(bugreportIn, archiveType);
            logsContainerToScan = new ArchiveLogsContainer(bugreportArchive);
        }

        var grepRule = new FileRule.GrepRule();
        grepRule.setName(r.getRegex());
        grepRule.setRegexes(List.of(r.getRegex()));
        grepRule.setType(RuleType.SIMPLE);
        grepRule.setShow(Integer.MAX_VALUE);
        grepRule.setShowAlways(true);
        grepRule.setTags(Set.of("regex"));
        grepRule.setFilters(EnumSet.noneOf(RuleFilter.class));
        var fileRule = new FileRule(r.getFile(), List.of(grepRule));

        var scanOptions = scanOptionsSupplier.get();
        scanOptions.setFileRules(List.of(fileRule));

        if (r.getDate() != null)
            scanOptions.setProblemDate(r.getDate());

        if (r.getFilters() != null)
            scanOptions.setFilters(EnumSet.copyOf(r.getFilters()));

        if (dateFormat != null)
            try (var dateFormatSteam = dateFormat.getInputStream()) {
                scanOptions.setDateFormat(configMapper.mapLogDateFormat(dateFormatSteam));
            }

        scanOptions.setYear(r.getYear());

        return scanner.scan(logsContainerToScan, scanOptions);
    }

    @Post("/rules")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Collection<String> rules(@Nullable CompletedFileUpload rules)
            throws IOException, InvalidConfigFormatException {

        if (rules == null)
            return listTags(internalRules);

        List<YamlRule> uploadedRules;
        try (var in = rules.getInputStream()) {
            uploadedRules = configMapper.mapRules(in);
        }

        return listTags(uploadedRules);
    }

    @Get("/filters")
    public RuleFilter[] filters() {
        return RuleFilter.values();
    }

    @Get()
    @View("index")
    public Map<String, String> main() {
        return Map.of(
                "title", app.getName(),
                "authors", app.getAuthors(),
                "_version", app.getVersion()
        );
    }

    @Get("/version")
    public Message version() {
        return new Message(HttpStatus.OK.getCode(), app.getVersion());
    }

    @Error
    public HttpResponse<Message> badRequest(BadRequestException e) {
        return HttpResponse.badRequest(new Message(HttpStatus.BAD_REQUEST.getCode(), e.getMessage()));
    }

    @Error
    public HttpResponse<Message> serverError(Throwable t) {
        return HttpResponse.serverError(new Message(HttpStatus.INTERNAL_SERVER_ERROR.getCode(), t.getMessage()));
    }

    private Collection<String> listTags(List<YamlRule> rules) {
        return rules
                .stream()
                .flatMap(r -> r.getTags().stream())
                .distinct()
                .sorted()
                .collect(Collectors.toUnmodifiableList());
    }

    private InputStream copyToMemoryAndClose(InputStream in) throws IOException {
        try (in) {
            return new ByteArrayInputStream(in.readAllBytes());
        }
    }

}
