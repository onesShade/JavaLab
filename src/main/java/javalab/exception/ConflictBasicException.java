package javalab.exception;

import org.springframework.http.HttpStatus;

public class ConflictBasicException extends BasicException {
    public ConflictBasicException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}