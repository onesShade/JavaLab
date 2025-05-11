package javalab.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import javalab.config.CacheHolder;
import javalab.exception.BadRequestException;
import javalab.exception.InternalException;
import javalab.exception.NotFoundException;
import javalab.logger.NoLogging;
import javalab.model.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncLogService {
    static final String LOG_FILE_PATH = "logs/app.log";
    private final File logFile;
    private final CacheHolder cacheHolder;
    static final DateTimeFormatter LOG_DATE_FORMATTER
            = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    public AsyncLogService(CacheHolder cacheHolder) {
        this.cacheHolder = cacheHolder;
        this.logFile = new File(LOG_FILE_PATH);
    }


    @SuppressWarnings("checkstyle:CatchParameterName")
    @Async("taskExecutor")
    public void createLogs(Long taskId, String date) {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Log log = new Log(taskId, Log.Status.FAILURE, date);
        cacheHolder.getLogFileCache().put(taskId, log);

        if (!logFile.exists()) {
            cacheHolder.getLogFileCache().put(taskId, log);
            throw new NotFoundException("Not found log file at " + logFile.getPath());
        }

        List<String> filteredLogs = filterLogsByDate(logFile, date);
        if (filteredLogs.isEmpty()) {
            cacheHolder.getLogFileCache().put(taskId, log);
            throw new NotFoundException("No logs found for " + date);
        }

        log.setStatus(Log.Status.SUCCESS);
        log.setBody(String.join("\n", filteredLogs));
        cacheHolder.getLogFileCache().put(taskId, log);
    }

    @NoLogging
    public List<String> filterLogsByDate(File logFile, String date) {
        List<String> filteredLogs = new ArrayList<>();
        LocalDate targetDate;
        try {
            targetDate = LocalDate.parse(date, LOG_DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new BadRequestException(e.getMessage());
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(targetDate.toString())) {
                    filteredLogs.add(line);
                }
            }
        } catch (IOException e) {
            throw new InternalException("Error filtering logs : " + e.getMessage());
        }

        return filteredLogs;
    }
}
