package javalab.exception;

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
    public ResponseEntity<ExceptionBody> handleNotFoundException(NotFoundException ex) {
        ExceptionBody eb = new ExceptionBody(ex.getMessage());
        return new ResponseEntity<>(eb, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ExceptionBody> handleBadRequestException(BadRequestException ex) {
        ExceptionBody eb = new ExceptionBody(ex.getMessage());
        return new ResponseEntity<>(eb, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ExceptionBody> handleConflictException(ConflictException ex) {
        ExceptionBody eb = new ExceptionBody(ex.getMessage());
        return new ResponseEntity<>(eb, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InternalException.class)
    public ResponseEntity<ExceptionBody> handleInternalException(InternalException ex) {
        ExceptionBody eb = new ExceptionBody(ex.getMessage());
        return new ResponseEntity<>(eb, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ExceptionBody> handleValidationException(ValidationException ex) {
        ExceptionBody eb = new ExceptionBody(ex.getMessage());
        return new ResponseEntity<>(eb, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionBody> handleOtherException(Exception ex) {
        ExceptionBody eb = new ExceptionBody(ex.getMessage());
        return new ResponseEntity<>(eb, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}