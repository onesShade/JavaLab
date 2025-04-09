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
import javalab.exception.BadRequestException;
import javalab.exception.InternalException;
import javalab.exception.NotFoundException;
import javalab.logger.NoLogging;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("java:S6829")
public class LogService {
    static final String LOG_FILE_PATH = "logs/app.log";
    static final DateTimeFormatter LOG_DATE_FORMATTER
            = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final File logFile;

    public LogService() {
        this(new File(LOG_FILE_PATH));
    }

    public LogService(File logFile) {
        this.logFile = logFile;
    }

    @NoLogging
    public String getLogsByDate(String date) {
        if (!logFile.exists()) {
            throw new NotFoundException("Not found log file at " + logFile.getPath());
        }

        List<String> filteredLogs = filterLogsByDate(logFile, date);
        if (filteredLogs.isEmpty()) {
            throw new NotFoundException("No logs found for " + date);
        }

        return String.join("\n", filteredLogs);
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