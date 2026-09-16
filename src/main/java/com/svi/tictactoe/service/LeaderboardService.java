package com.svi.tictactoe.service;

import com.svi.tictactoe.domain.Player;
import com.svi.tictactoe.dto.response.leaderboard.LeaderboardResponse;

/**
 * Defines operations for reading and updating the global player leaderboard.
 */
public interface LeaderboardService {

    /**
     * Retrieves the globally ranked leaderboard.
     *
     * @return the current leaderboard
     */
    LeaderboardResponse getLeaderboard();

    /**
     * Recalculates and persists the leaderboard entry for a player.
     *
     * @param player the player whose entry is updated
     */
    void updatePlayer(Player player);

    /**
     * Recalculates and persists leaderboard entries for multiple players.
     *
     * @param players the players whose entries are updated
     */
    void updatePlayers(Player... players);
}
