package com.svi.tictactoe.controller;

import com.svi.tictactoe.dto.request.game.CreateGameRequest;
import com.svi.tictactoe.dto.request.room.CreateRoomRequest;
import com.svi.tictactoe.dto.request.room.JoinRoomRequest;
import com.svi.tictactoe.dto.response.room.RoomResponse;
import com.svi.tictactoe.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Exposes REST operations for creating rooms and managing their players and games.
 */
@RestController
@RequestMapping("/api/v1/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    /**
     * Creates a waiting room for a player.
     *
     * @param request the room creation details
     * @return a response containing the created room with status 201 Created
     * @throws com.svi.tictactoe.exception.ResourceNotFoundException if the owner does not exist
     * @throws com.svi.tictactoe.exception.IllegalGameStateException if the owner is already in an active room
     */
    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.createRoom(request));
    }

    /**
     * Retrieves a room by its code.
     *
     * @param roomCode the room code
     * @return a response containing the room with status 200 OK
     * @throws com.svi.tictactoe.exception.ResourceNotFoundException if the room does not exist
     */
    @GetMapping("/{roomCode}")
    public ResponseEntity<RoomResponse> getRoom(@PathVariable String roomCode) {
        return ResponseEntity.ok(roomService.getRoom(roomCode));
    }

    /**
     * Adds a guest to a waiting room and starts its first game.
     *
     * @param roomCode the room code
     * @param request the joining player's details
     * @return a response containing the updated room with status 200 OK
     * @throws com.svi.tictactoe.exception.ResourceNotFoundException if the player or room does not exist
     * @throws com.svi.tictactoe.exception.IllegalGameStateException if the player or room is not eligible for joining
     */
    @PostMapping("/{roomCode}/players")
    public ResponseEntity<RoomResponse> joinRoom(@PathVariable String roomCode, @Valid @RequestBody JoinRoomRequest request) {
        return ResponseEntity.ok(roomService.joinRoom(roomCode, request));
    }

    /**
     * Closes a room when one of its players leaves.
     *
     * @param roomCode the room code
     * @param playerId the leaving player's identifier
     * @return a response containing the closed room with status 200 OK
     * @throws com.svi.tictactoe.exception.ResourceNotFoundException if the room does not exist
     * @throws com.svi.tictactoe.exception.IllegalGameStateException if the player is not in the room or the room is already closed
     */
    @DeleteMapping("/{roomCode}/players/{playerId}")
    public ResponseEntity<RoomResponse> leaveRoom(@PathVariable String roomCode, @PathVariable UUID playerId) {
        return ResponseEntity.ok(roomService.leaveRoom(roomCode, playerId));
    }

    /**
     * Starts another game for a room whose previous game has finished.
     *
     * @param roomCode the room code
     * @param request the requesting player's details
     * @return a response containing the updated room with status 200 OK
     * @throws com.svi.tictactoe.exception.ResourceNotFoundException if the room does not exist
     * @throws com.svi.tictactoe.exception.IllegalGameStateException if the requester is not the owner or the room is not ready
     */
    @PostMapping("/{roomCode}/games")
    public ResponseEntity<RoomResponse> createGame(@PathVariable String roomCode, @Valid @RequestBody CreateGameRequest request) {
        return ResponseEntity.ok(roomService.createGame(roomCode, request));
    }
}
