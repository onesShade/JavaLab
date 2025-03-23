package javalab.exception;

import org.springframework.http.HttpStatus;

public class MethodArgumentNotValidException extends BasicException {
    public MethodArgumentNotValidException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}