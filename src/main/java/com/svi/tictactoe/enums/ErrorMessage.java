package com.svi.tictactoe.enums;

public enum ErrorMessage {

    CELL_ALREADY_OCCUPIED("The selected cell is already occupied."),
    INVALID_BOARD_POSITION("Row and column must be between 0 and %d."),
    PLAYER_NOT_FOUND("Player with ID %s was not found."),
    ROOM_NOT_FOUND("Room with code %s was not found."),
    PLAYER_ALREADY_IN_ACTIVE_ROOM("Player with ID %s is already in an active room."),
    PLAYER_CANNOT_JOIN_OWN_ROOM("The room owner cannot join as the guest."),
    ROOM_NOT_AVAILABLE("Room %s is not available for joining."),
    GAME_NOT_FOUND("Game with ID %s was not found."),
    GAME_NOT_IN_PROGRESS("Game with ID %s is not in progress."),
    PLAYER_NOT_IN_GAME("Player with ID %s is not part of game %s."),
    NOT_PLAYER_TURN("It is not player %s's turn."),
    PLAYER_NOT_IN_ROOM("Player with ID %s is not part of room %s."),
    ROOM_ALREADY_CLOSED("Room %s is already closed."),
    ONLY_ROOM_OWNER_CAN_REMATCH("Only the room owner can request a rematch."),
    REMATCH_NOT_AVAILABLE("Room %s is not ready for a rematch.");

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