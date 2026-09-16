package com.svi.tictactoe.exception;

/**
 * Thrown when a move targets an invalid board position or an occupied cell.
 */
public class InvalidMoveException extends RuntimeException {

    public InvalidMoveException(String message) {
        super(message);
    }
}
