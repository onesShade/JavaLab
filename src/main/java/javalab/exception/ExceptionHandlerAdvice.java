package javalab.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(
        basePackages = "javalab.controller",
        annotations = ExceptionHandler.class
)
public class ExceptionHandlerAdvice {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFoundException(NotFoundException ex) {
        return ex.getResponseEntity();
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, String>> handleBadRequestException(BadRequestException ex) {
        return ex.getResponseEntity();
    }

    @ExceptionHandler(ConflictBasicException.class)
    public ResponseEntity<Map<String, String>> handleConflictException(ConflictBasicException ex) {
        return ex.getResponseEntity();
    }

    @ExceptionHandler(InternalException.class)
    public ResponseEntity<Map<String, String>> handleInternalException(InternalException ex) {
        return ex.getResponseEntity();
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleOtherException(Exception ex) {
        return new BasicException(ex.getMessage(), HttpStatus.BAD_REQUEST).getResponseEntity();
    }
}