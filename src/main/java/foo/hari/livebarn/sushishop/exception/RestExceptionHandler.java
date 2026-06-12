package foo.hari.livebarn.sushishop.exception;

import foo.hari.livebarn.sushishop.dto.GenericResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<GenericResponseDTO> handleRuntimeException(RuntimeException ex) {
        return new ResponseEntity<>(
                new GenericResponseDTO(HttpStatus.NOT_FOUND.value(), ex.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

}
