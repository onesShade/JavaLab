package javalab.exception;

import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class ExceptionHandlerAdvice {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Map<String, Object>> handleNotFoundException(NotFoundException ex) {
        return ex.getResponseEntity();
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleBadRequestException(BadRequestException ex) {
        return ex.getResponseEntity();
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<Map<String, Object>> handleConflictException(ConflictException ex) {
        return ex.getResponseEntity();
    }

    @ExceptionHandler(InternalException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Map<String, Object>> handleInternalException(InternalException ex) {
        return ex.getResponseEntity();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>>
            handleConstraintViolationException(ConstraintViolationException ex) {
        List<String> message = ex.getConstraintViolations().stream()
                .map(violation ->
                        violation.getPropertyPath().toString() + ": " + violation.getMessage())
                .toList();

        return new BasicException(message, HttpStatus.BAD_REQUEST).getResponseEntity();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>>
            handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        List<String> message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        return new BasicException(message, HttpStatus.BAD_REQUEST).getResponseEntity();
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>>
            handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String message = String.format(
                "Parameter '%s' has invalid value: '%s'. Expected type: %s",
                ex.getName(),
                ex.getValue(),
                ex.getRequiredType() != null ? ex.getRequiredType() : "unknown"
        );

        return new BasicException(message, HttpStatus.BAD_REQUEST).getResponseEntity();
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex) {
        return new BasicException(ex.getMessage(),
                HttpStatus.BAD_REQUEST).getResponseEntity();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Map<String, Object>> handleOtherException(Exception ex) {
        return new BasicException(ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR).getResponseEntity();
    }
}