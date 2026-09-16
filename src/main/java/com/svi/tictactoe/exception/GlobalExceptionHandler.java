package com.svi.tictactoe.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;

/**
 * Centralizes translation of application and request exceptions into consistent HTTP error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles requests for application resources that do not exist.
     *
     * @param exception the resource lookup failure
     * @return an error response with status 404 Not Found
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException exception) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    /**
     * Handles operations that conflict with the current room or game state.
     *
     * @param exception the invalid state transition
     * @return an error response with status 409 Conflict
     */
    @ExceptionHandler(IllegalGameStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalGameState(IllegalGameStateException exception) {
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage());
    }

    /**
     * Handles invalid board positions and moves into occupied cells.
     *
     * @param exception the invalid move
     * @return an error response with status 400 Bad Request
     */
    @ExceptionHandler(InvalidMoveException.class)
    public ResponseEntity<ErrorResponse> handleInvalidMove(InvalidMoveException exception) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    /**
     * Handles request bodies that fail bean validation.
     *
     * @param exception the validation failure
     * @return an error response with status 400 Bad Request
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Validation failed.");
        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * Handles unexpected exceptions not matched by a more specific handler.
     *
     * @param exception the unexpected failure
     * @return a generic error response with status 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
    }

    /**
     * Handles malformed request bodies and incompatible argument types.
     *
     * @param exception the request parsing or conversion failure
     * @return an error response with status 400 Bad Request
     */
    @ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})

    public ResponseEntity<ErrorResponse> handleBadRequest(Exception exception) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid request.");
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message) {
        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message
        );
        return ResponseEntity.status(status).body(response);
    }
}
