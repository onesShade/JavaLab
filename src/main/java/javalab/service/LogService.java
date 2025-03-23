package javalab.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javalab.exception.InternalException;
import javalab.exception.NotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class LogService {
    private static final String LOG_FILE_PATH = "logs/app.log";
    private static final DateTimeFormatter LOG_DATE_FORMATTER
            = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    public ResponseEntity<String> getLogsByDate(String date) {
        File logFile = new File(LOG_FILE_PATH);
        if (!logFile.exists()) {
            throw new NotFoundException("Not found log file at " + LOG_FILE_PATH);
        }

        List<String> filteredLogs = filterLogsByDate(logFile, date);
        if (filteredLogs.isEmpty()) {
            throw new NotFoundException("No logs found for " + date);
        }

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"logs-" + date + ".txt\"")
                .body(String.join("\n", filteredLogs));
    }

    private List<String> filterLogsByDate(File logFile, String date) {
        List<String> filteredLogs = new ArrayList<>();
        LocalDate targetDate = LocalDate.parse(date, LOG_DATE_FORMATTER);

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
