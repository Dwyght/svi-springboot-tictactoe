package com.svi.tictactoe.exception;

/**
 * Thrown when an operation is incompatible with the current room, game, or player state.
 */
public class IllegalGameStateException extends RuntimeException {

    public IllegalGameStateException(String message) {
        super(message);
    }
}
