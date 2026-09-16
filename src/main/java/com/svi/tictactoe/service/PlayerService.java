package com.svi.tictactoe.service;

import com.svi.tictactoe.dto.request.player.CreatePlayerRequest;
import com.svi.tictactoe.dto.response.player.PlayerResponse;

import java.util.UUID;

/**
 * Defines operations for registering players and retrieving their current records.
 */
public interface PlayerService {

    /**
     * Registers a player with initial game statistics.
     *
     * @param request the player registration details
     * @return the created player
     */
    PlayerResponse createPlayer(CreatePlayerRequest request);

    /**
     * Retrieves a player by identifier.
     *
     * @param playerId the player identifier
     * @return the requested player
     * @throws com.svi.tictactoe.exception.ResourceNotFoundException if the player does not exist
     */
    PlayerResponse getPlayer(UUID playerId);
}
