package com.svi.tictactoe.dto.response.player;

import java.util.UUID;

public record PlayerResponse(
        UUID playerId,
        String name,
        int wins,
        int losses,
        int draws,
        int gamesPlayed
        ) {
}