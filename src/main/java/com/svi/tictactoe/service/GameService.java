package com.svi.tictactoe.service;

import com.svi.tictactoe.dto.request.game.MakeMoveRequest;
import com.svi.tictactoe.dto.response.game.GameResponse;

import java.util.UUID;

/**
 * Defines operations for retrieving games, making moves, and resolving forfeits.
 */
public interface GameService {

    /**
     * Retrieves the current state of a game.
     *
     * @param gameId the game identifier
     * @return the current game state
     * @throws com.svi.tictactoe.exception.ResourceNotFoundException if the game does not exist
     */
    GameResponse getGame(UUID gameId);

    /**
     * Applies a player's move to an active game.
     *
     * @param gameId the game identifier
     * @param request the player and board position for the move
     * @return the updated game state
     * @throws com.svi.tictactoe.exception.ResourceNotFoundException if the game does not exist
     * @throws com.svi.tictactoe.exception.IllegalGameStateException if the game or player is not eligible for the move
     * @throws com.svi.tictactoe.exception.InvalidMoveException if the selected board position is invalid or occupied
     */
    GameResponse makeMove(UUID gameId, MakeMoveRequest request);

    /**
     * Finishes an active game as a forfeit by the specified player.
     *
     * @param gameId the game identifier
     * @param forfeitingPlayerId the identifier of the forfeiting player
     * @throws com.svi.tictactoe.exception.ResourceNotFoundException if the game does not exist
     * @throws com.svi.tictactoe.exception.IllegalGameStateException if the player does not belong to the game
     */
    void forfeitGame(UUID gameId, UUID forfeitingPlayerId);
}
