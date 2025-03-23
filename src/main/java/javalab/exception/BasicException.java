package javalab.exception;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public class BasicException extends RuntimeException {
    private final LocalDateTime timestamp;
    private final HttpStatus status;
    private final List<String> messages; // Изменил на список строк

    public BasicException(String message, HttpStatus status) {
        super(message);
        this.messages = Arrays.asList(message
                .replace("\t", " ")
                .split("\\n"));
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    public BasicException(List<String> messages, HttpStatus status) {
        this.messages = messages;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    public ResponseEntity<Map<String, Object>> getResponseEntity() {
        Map<String, Object> map = new HashMap<>();
        map.put("timestamp", timestamp.toString());
        map.put("status", status.toString());
        map.put("messages", messages); // Возвращаем список сообщений
        return new ResponseEntity<>(map, status);
    }
}