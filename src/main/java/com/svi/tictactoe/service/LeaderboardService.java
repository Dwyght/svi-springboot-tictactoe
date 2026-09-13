package com.svi.tictactoe.service;

import com.svi.tictactoe.domain.Player;
import com.svi.tictactoe.dto.response.leaderboard.LeaderboardResponse;

public interface LeaderboardService {

    LeaderboardResponse getLeaderboard();
    void updatePlayer(Player player);
}