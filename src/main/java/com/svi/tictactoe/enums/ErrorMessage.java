package com.svi.tictactoe.enums;

public enum ErrorMessage {

    CELL_ALREADY_OCCUPIED("The selected cell is already occupied."),
    INVALID_BOARD_POSITION("Row and column must be between 0 and %d."),
    PLAYER_NOT_FOUND("Player with ID %s was not found.");

    private final String message;

    ErrorMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String format(Object... args) {
        return message.formatted(args);
    }
}