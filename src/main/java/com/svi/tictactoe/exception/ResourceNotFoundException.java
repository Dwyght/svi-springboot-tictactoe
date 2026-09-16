package com.svi.tictactoe.exception;

/**
 * Thrown when a requested player, room, or game cannot be found.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
