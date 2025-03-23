package javalab.exception;

import org.springframework.http.HttpStatus;

public class InternalException extends BasicException {
    public InternalException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}