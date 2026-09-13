package com.svi.tictactoe.dto.response.leaderboard;

import java.util.UUID;

public record LeaderboardEntryResponse(
        int rank,
        UUID playerId,
        String playerName,
        int wins,
        int losses,
        int draws,
        int gamesPlayed,
        double score
) {
}