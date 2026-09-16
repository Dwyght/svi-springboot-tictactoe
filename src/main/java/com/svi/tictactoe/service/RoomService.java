package com.svi.tictactoe.service;

import com.svi.tictactoe.dto.request.room.CreateRoomRequest;
import com.svi.tictactoe.dto.request.room.JoinRoomRequest;
import com.svi.tictactoe.dto.request.game.CreateGameRequest;
import com.svi.tictactoe.dto.response.room.RoomResponse;

import java.util.UUID;

/**
 * Defines room lifecycle operations, including joining, leaving, and starting games.
 */
public interface RoomService {

    /**
     * Creates a waiting room owned by the requested player.
     *
     * @param request the room creation details
     * @return the created room
     * @throws com.svi.tictactoe.exception.ResourceNotFoundException if the owner does not exist
     * @throws com.svi.tictactoe.exception.IllegalGameStateException if the owner is already in an active room
     */
    RoomResponse createRoom(CreateRoomRequest request);

    /**
     * Retrieves a room by its room code.
     *
     * @param roomCode the room code
     * @return the requested room
     * @throws com.svi.tictactoe.exception.ResourceNotFoundException if the room does not exist
     */
    RoomResponse getRoom(String roomCode);

    /**
     * Adds a guest to a waiting room and starts its first game.
     *
     * @param roomCode the room code
     * @param request the joining player's details
     * @return the updated room
     * @throws com.svi.tictactoe.exception.ResourceNotFoundException if the player or room does not exist
     * @throws com.svi.tictactoe.exception.IllegalGameStateException if the player or room is not eligible for joining
     */
    RoomResponse joinRoom(String roomCode, JoinRoomRequest request);

    /**
     * Removes a participating player by closing the room and forfeiting any active game.
     *
     * @param roomCode the room code
     * @param playerId the leaving player's identifier
     * @return the closed room
     * @throws com.svi.tictactoe.exception.ResourceNotFoundException if the room does not exist
     * @throws com.svi.tictactoe.exception.IllegalGameStateException if the player is not in the room or the room is already closed
     */
    RoomResponse leaveRoom(String roomCode, UUID playerId);

    /**
     * Starts another game for the players in a room whose previous game has finished.
     *
     * @param roomCode the room code
     * @param request the requesting player's details
     * @return the room updated with the new game
     * @throws com.svi.tictactoe.exception.ResourceNotFoundException if the room does not exist
     * @throws com.svi.tictactoe.exception.IllegalGameStateException if the requester is not the owner or the room is not ready
     */
    RoomResponse createGame(String roomCode, CreateGameRequest request);
}
