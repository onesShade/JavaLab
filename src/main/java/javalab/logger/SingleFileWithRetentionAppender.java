package javalab.logger;

import ch.qos.logback.core.FileAppender;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import javalab.exception.NotFoundException;
import lombok.Setter;

@Setter
public class SingleFileWithRetentionAppender<E> extends FileAppender<E> {

    private static final int MAX_HISTORY = 7; // Хранение логов за 7 дней

    @Override
    public void start() {
        super.start();
        cleanOldLogs();
    }

    private void cleanOldLogs() {
        File logFile = new File(getFile());
        if (!logFile.exists()) {
            throw new NotFoundException("Log file not found");
        }

        try {
            Path path = Paths.get(getFile());
            List<String> lines = Files.readAllLines(path);

            LocalDate now = LocalDate.now();
            List<String> filteredLines = lines.stream()
                    .filter(line -> {
                        try {
                            String dateString = line.substring(0, 10); // "yyyy-MM-dd"
                            LocalDate logDate = LocalDate.parse(dateString);
                            return ChronoUnit.DAYS.between(logDate, now) <= MAX_HISTORY;
                        } catch (Exception e) {
                            return true;
                        }
                    })
                    .toList();
            Files.write(path, filteredLines);
        } catch (IOException e) {
            addError("Failed to clean old logs: " + e.getMessage(), e);
        }
    }
}