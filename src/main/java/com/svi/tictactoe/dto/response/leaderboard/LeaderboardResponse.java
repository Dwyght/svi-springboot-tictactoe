package com.svi.tictactoe.dto.response.leaderboard;

import java.util.List;

public record LeaderboardResponse(
        List<LeaderboardEntryResponse> entries
) {
}