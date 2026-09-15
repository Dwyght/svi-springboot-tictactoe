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

@RestController
@RequestMapping("/api/v1/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.createRoom(request));
    }

    @GetMapping("/{roomCode}")
    public ResponseEntity<RoomResponse> getRoom(@PathVariable String roomCode) {
        return ResponseEntity.ok(roomService.getRoom(roomCode));
    }

    @PostMapping("/{roomCode}/players")
    public ResponseEntity<RoomResponse> joinRoom(@PathVariable String roomCode, @Valid @RequestBody JoinRoomRequest request) {
        return ResponseEntity.ok(roomService.joinRoom(roomCode, request));
    }

    @DeleteMapping("/{roomCode}/players/{playerId}")
    public ResponseEntity<RoomResponse> leaveRoom(@PathVariable String roomCode, @PathVariable UUID playerId) {
        return ResponseEntity.ok(roomService.leaveRoom(roomCode, playerId));
    }

    @PostMapping("/{roomCode}/games")
    public ResponseEntity<RoomResponse> createGame(@PathVariable String roomCode, @Valid @RequestBody CreateGameRequest request) {
        return ResponseEntity.ok(roomService.createGame(roomCode, request));
    }
}
