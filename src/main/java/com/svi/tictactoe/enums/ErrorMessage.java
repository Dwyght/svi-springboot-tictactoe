package com.svi.tictactoe.enums;

public enum ErrorMessage {

    CELL_ALREADY_OCCUPIED("The selected cell is already occupied."),
    INVALID_BOARD_POSITION("Row and column must be between 0 and %d."),
    PLAYER_NOT_FOUND("Player with ID %s was not found."),
    ROOM_NOT_FOUND("Room with code %s was not found."),
    PLAYER_ALREADY_IN_ACTIVE_ROOM("Player with ID %s is already in an active room."),
    PLAYER_CANNOT_JOIN_OWN_ROOM("The room owner cannot join as the guest."),
    ROOM_NOT_AVAILABLE("Room %s is not available for joining.");

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