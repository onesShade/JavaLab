package javalab.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends BasicException {
    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}