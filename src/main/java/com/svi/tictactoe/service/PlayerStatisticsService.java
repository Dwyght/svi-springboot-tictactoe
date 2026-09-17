package com.svi.tictactoe.service;

import java.util.UUID;

/**
 * Defines operations for recording completed-game results in player statistics and the leaderboard.
 */
public interface PlayerStatisticsService {

    /**
     * Records a win for one player and a loss for the other player.
     *
     * @param winnerId the winning player's identifier
     * @param loserId the losing player's identifier
     */
    void recordWinLoss(UUID winnerId, UUID loserId);

    /**
     * Records a draw for both players.
     *
     * @param playerXId the Player X identifier
     * @param playerOId the Player O identifier
     */
    void recordDraw(UUID playerXId, UUID playerOId);
}
