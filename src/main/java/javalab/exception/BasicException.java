package javalab.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public class BasicException extends RuntimeException {
    private final LocalDateTime timestamp;
    private final HttpStatus status;
    private final String message;

    public BasicException(String message, HttpStatus status) {
        super(message);
        this.message = message;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    public ResponseEntity<Map<String, String>> getResponseEntity() {
        Map<String, String> map = new HashMap<>();
        map.put("timestamp", timestamp.toString());
        map.put("status", status.toString());
        map.put("message", message);
        return new ResponseEntity<>(map, status);
    }
}