package com.svi.tictactoe.constant;

import java.util.UUID;

public final class RealtimeTopic {

    public static final String ROOM_PREFIX = "/topic/rooms/";
    public static final String GAME_PREFIX = "/topic/games/";
    public static final String LEADERBOARD = "/topic/leaderboard";
    public static final String LOBBY = "/topic/lobby";

    private RealtimeTopic() {
    }

    public static String room(String roomCode) {
        return ROOM_PREFIX + roomCode;
    }

    public static String game(UUID gameId) {
        return GAME_PREFIX + gameId;
    }
}
