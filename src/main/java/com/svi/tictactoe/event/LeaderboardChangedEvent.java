package com.svi.tictactoe.event;

import com.svi.tictactoe.dto.response.leaderboard.LeaderboardResponse;

public record LeaderboardChangedEvent(
        LeaderboardResponse leaderboard
) {
}