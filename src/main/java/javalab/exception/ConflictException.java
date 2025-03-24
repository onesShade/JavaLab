package javalab.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends BasicException {
    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}