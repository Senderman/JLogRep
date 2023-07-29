package com.senderman.jlogrep.controller;

import com.senderman.jlogrep.archive.Archive;
import com.senderman.jlogrep.config.ConfigMapper;
import com.senderman.jlogrep.container.ArchiveLogsContainer;
import com.senderman.jlogrep.container.LogsContainer;
import com.senderman.jlogrep.container.PlainTextLogsContainer;
import com.senderman.jlogrep.exception.EmptyBugreportNameException;
import com.senderman.jlogrep.exception.NoSuchTaskException;
import com.senderman.jlogrep.model.internal.ScanOptions;
import com.senderman.jlogrep.model.request.AbsAnalysisRequest;
import com.senderman.jlogrep.model.request.RegexRequest;
import com.senderman.jlogrep.model.request.ScanRequest;
import com.senderman.jlogrep.model.response.FileInfo;
import com.senderman.jlogrep.model.response.Message;
import com.senderman.jlogrep.model.response.Problem;
import com.senderman.jlogrep.model.rule.GrepRule;
import com.senderman.jlogrep.model.rule.RuleFilter;
import com.senderman.jlogrep.model.rule.RuleType;
import com.senderman.jlogrep.scanner.FileListScanner;
import com.senderman.jlogrep.scanner.LogScanner;
import com.senderman.jlogrep.task.TaskManager;
import com.senderman.jlogrep.task.TaskRepresentation;
import com.senderman.jlogrep.util.ConfigConverter;
import com.senderman.jlogrep.util.Uploads;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.format.Format;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Provider;

import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Controller
public class MainController {

    private static final int REGEX_DEFAULT_SHOW_VALUE = 5;

    private final LogScanner logScanner;
    private final FileListScanner fileListScanner;
    private final TaskManager<Collection<Problem>> problemTaskManager;
    private final TaskManager<Collection<FileInfo>> fileInfoTaskManager;
    private final Provider<ScanOptions> defaultScanOptions;
    private final ConfigMapper configMapper;

    public MainController(
            LogScanner logScanner,
            FileListScanner fileListScanner,
            TaskManager<Collection<Problem>> problemTaskManager,
            TaskManager<Collection<FileInfo>> fileInfoTaskManager,
            Provider<ScanOptions> defaultScanOptions,
            ConfigMapper configMapper
    ) {
        this.logScanner = logScanner;
        this.fileListScanner = fileListScanner;
        this.problemTaskManager = problemTaskManager;
        this.fileInfoTaskManager = fileInfoTaskManager;
        this.defaultScanOptions = defaultScanOptions;
        this.configMapper = configMapper;
    }

    @Post("/scan")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(
            summary = "Scan logs by rules",
            description = "Send logs to scan them with internal or provided rules. Poll the result by taskId on /result-problems"
    )
    @ApiResponse(responseCode = "200")
    @ApiResponse(
            responseCode = "400",
            description = "Empty bugreport name, or invalid config (rules/dateformat) format",
            content = @Content(schema = @Schema(implementation = Message.class))
    )
    @ApiResponse(
            responseCode = "500",
            description = "Other error",
            content = @Content(schema = @Schema(implementation = Message.class))
    )
    public TaskRepresentation<Collection<Problem>> scan(
            @Parameter(description = "Logs to scan")
            CompletedFileUpload bugreport,

            @Parameter(description = "Rules to scan logs with. Internal if not supplied")
            @Nullable
            CompletedFileUpload rules,

            @Parameter(description = "Date format to use. Internal if not supplied")
            @Nullable
            CompletedFileUpload dateFormat,

            @Parameter(
                    description = "Date of the problem. Scan all logs if not supplied. Format: " + AbsAnalysisRequest.DATE_FORMAT
            )
            @Format(AbsAnalysisRequest.DATE_FORMAT)
            @Header @Nullable
            String when,

            @Parameter(description = "This year will be used for logs without year. Current if not supplied")
            @Header @Nullable
            Integer year,

            @Parameter(
                    description = "Defines upper and lower bounds from the date of the problem to scan, in minutes",
                    schema = @Schema(defaultValue = "10", implementation = Integer.class)
            )
            @Header @Nullable
            Integer interval,

            @Parameter(description = "Filters to apply, separated by comma")
            @Header @Nullable
            String filters,

            @Parameter(description = "Which types of problems to scan for, separated by comma")
            @Header
            @Nullable String tags
    ) throws IOException {

        var r = new ScanRequest(when, year, interval, filters, tags);

        LogsContainer logsContainerToScan = createLogsContainer(bugreport);
        var scanOptions = defaultScanOptions.get();

        if (rules != null)
            scanOptions.setConfigRules(configMapper.mapRules(rules.getInputStream()));

        if (dateFormat != null)
            scanOptions.setDateFormat(ConfigConverter.toLogDateFormat(configMapper.mapLogDateFormat(dateFormat.getInputStream())));

        scanOptions
                .setProblemDate(r.getDate())
                .setYear(r.getYear())
                .setInterval(r.getInterval())
                .setTags(r.getTags())
                .setFilters(r.getFilters());

        return problemTaskManager.addTask(() -> logScanner.scan(logsContainerToScan, scanOptions));
    }

    @Post("/regex")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(
            summary = "Scan logs by regex",
            description = "Send logs to scan them with provided regex. Poll the result by taskId on /result-problems"
    )
    @ApiResponse(responseCode = "200")
    @ApiResponse(
            responseCode = "400",
            description = "Empty bugreport name, or invalid config (dateformat) format",
            content = @Content(schema = @Schema(implementation = Message.class))
    )
    @ApiResponse(
            responseCode = "500",
            description = "Other error",
            content = @Content(schema = @Schema(implementation = Message.class))
    )
    public TaskRepresentation<Collection<Problem>> regex(
            @Parameter(description = "Logs to scan")
            CompletedFileUpload bugreport,

            @Parameter(description = "Date format to use. Internal if not supplied")
            @Nullable CompletedFileUpload dateFormat,

            @Parameter(
                    description = "Date of the problem. Scan all logs if not supplied. Format: " + AbsAnalysisRequest.DATE_FORMAT
            )
            @Format(AbsAnalysisRequest.DATE_FORMAT)
            @Header @Nullable
            String when,

            @Parameter(description = "This year will be used for logs without year. Current if not supplied")
            @Header @Nullable
            Integer year,

            @Parameter(
                    description = "Defines upper and lower bounds from the date of the problem to scan, in minutes",
                    schema = @Schema(defaultValue = "10", implementation = Integer.class)
            )
            @Header @Nullable
            Integer interval,

            @Parameter(description = "Filters to apply, separated by comma")
            @Header @Nullable
            String filters,

            @Parameter(description = "Regex to scan logs with")
            @Header
            String regex,

            @Parameter(
                    description = "Word that files to be scanned should contain. All files if not supplied",
                    schema = @Schema(defaultValue = "*", implementation = String.class)
            )
            @Header @Nullable
            String file
    ) throws IOException {

        var r = new RegexRequest(when, year, interval, filters, regex, file);

        LogsContainer logsContainerToScan = createLogsContainer(bugreport);
        var grepRule = new GrepRule(
                r.getFile(),
                String.format("regex: %s, file: %s", r.getRegex(), r.getFile()),
                RuleType.SIMPLE,
                REGEX_DEFAULT_SHOW_VALUE,
                false,
                List.of(Pattern.compile(r.getRegex())),
                "regex",
                EnumSet.noneOf(RuleFilter.class)
        );

        var scanOptions = defaultScanOptions.get();

        if (dateFormat != null)
            scanOptions.setDateFormat(ConfigConverter.toLogDateFormat(configMapper.mapLogDateFormat(dateFormat.getInputStream())));

        scanOptions
                .setRules(List.of(grepRule))
                .setProblemDate(r.getDate())
                .setYear(r.getYear())
                .setInterval(r.getInterval())
                .setTags(Set.of("regex"))
                .setFilters(EnumSet.copyOf(r.getFilters()));

        return problemTaskManager.addTask(() -> logScanner.scan(logsContainerToScan, scanOptions));
    }

    @Post("/files")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiResponse(responseCode = "200")
    @ApiResponse(
            responseCode = "500",
            description = "Other error",
            content = @Content(schema = @Schema(implementation = Message.class))
    )
    @Operation(
            summary = "Get info about files in archive",
            description = "Info about file names and archive (recursive unpacking). If unable to unpack the supplied file, get info about this file"
    )
    public TaskRepresentation<Collection<FileInfo>> files(
            @Parameter(description = "File to scan")
            CompletedFileUpload bugreport
    ) throws IOException {
        LogsContainer logsContainerToScan = createLogsContainer(bugreport);
        return fileInfoTaskManager.addTask(() -> fileListScanner.scan(logsContainerToScan));
    }

    @Get("/result-problems")
    @Operation(
            summary = "Get result of /scan or /regex request",
            description = "Use this method to pool result of /scan or /regex request"
    )
    @ApiResponse(responseCode = "200")
    @ApiResponse(
            responseCode = "404",
            description = "No task with given id found",
            content = @Content(schema = @Schema(implementation = Message.class))
    )
    @ApiResponse(
            responseCode = "500",
            description = "Other error",
            content = @Content(schema = @Schema(implementation = Message.class))
    )
    public TaskRepresentation<Collection<Problem>> resultProblems(
            @Parameter(description = "Id of the task")
            @QueryValue("taskId") String taskId
    ) throws NoSuchTaskException {
        return problemTaskManager.getTaskAndDeleteIfReady(taskId);
    }

    @Get("/result-files")
    @Operation(
            summary = "Get result of /files request",
            description = "Use this method to pool result of /files request"
    )
    @ApiResponse(responseCode = "200")
    @ApiResponse(
            responseCode = "404",
            description = "No task with given id found",
            content = @Content(schema = @Schema(implementation = Message.class))
    )
    @ApiResponse(
            responseCode = "500",
            description = "Other error",
            content = @Content(schema = @Schema(implementation = Message.class))
    )
    public TaskRepresentation<Collection<FileInfo>> resultFiles(
            @Parameter(description = "Id of the task")
            @QueryValue("taskId") String taskId
    ) throws NoSuchTaskException {
        return fileInfoTaskManager.getTaskAndDeleteIfReady(taskId);
    }

    private LogsContainer createLogsContainer(CompletedFileUpload upload) throws IOException, EmptyBugreportNameException {
        var filename = upload.getFilename();
        if (filename == null)
            throw new EmptyBugreportNameException();

        var archiveCreator = Archive.getArchiveCreator(filename);

        if (archiveCreator == null) // process as text if null
            return new PlainTextLogsContainer(Uploads.copy(upload), filename, upload.getSize());
        else
            return new ArchiveLogsContainer(archiveCreator.apply(filename, Uploads.copy(upload)));
    }

}
