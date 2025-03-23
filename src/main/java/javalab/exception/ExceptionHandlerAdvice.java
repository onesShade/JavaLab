package javalab.exception;

import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice(
        basePackages = "javalab.controller",
        annotations = ExceptionHandler.class
)
public class ExceptionHandlerAdvice {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFoundException(NotFoundException ex) {
        return ex.getResponseEntity();
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequestException(BadRequestException ex) {
        return ex.getResponseEntity();
    }

    @ExceptionHandler(ConflictBasicException.class)
    public ResponseEntity<Map<String, Object>> handleConflictException(ConflictBasicException ex) {
        return ex.getResponseEntity();
    }

    @ExceptionHandler(InternalException.class)
    public ResponseEntity<Map<String, Object>> handleInternalException(InternalException ex) {
        return ex.getResponseEntity();
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleOtherException(Exception ex) {
        return new BasicException(ex.getMessage(), HttpStatus.BAD_REQUEST).getResponseEntity();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>>
            handleConstraintViolationException(ConstraintViolationException ex) {
        List<String> message = ex.getConstraintViolations().stream()
                .map(violation ->
                        violation.getPropertyPath().toString() + ": " + violation.getMessage())
                .toList();

        return new BasicException(message, HttpStatus.BAD_REQUEST).getResponseEntity();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>>
            handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        List<String> message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        return new BasicException(message, HttpStatus.BAD_REQUEST).getResponseEntity();
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
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
}