package com.svi.tictactoe.constant;

public final class GameConstants {

    public static final int BOARD_SIZE = 3;
    public static final int ROOM_CODE_LENGTH = 8;
    public static final String ROOM_CODE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String ROOM_CODE_PATTERN = "^[" + ROOM_CODE_CHARACTERS + "]{" + ROOM_CODE_LENGTH + "}$";

    private GameConstants() {
    }
}
