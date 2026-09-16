package com.svi.tictactoe.controller;

import com.svi.tictactoe.dto.request.player.CreatePlayerRequest;
import com.svi.tictactoe.dto.response.player.PlayerResponse;
import com.svi.tictactoe.service.PlayerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Exposes REST operations for registering and retrieving players.
 */
@RestController
@RequestMapping("/api/v1/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    /**
     * Registers a player from a validated request.
     *
     * @param request the player registration details
     * @return a response containing the created player with status 201 Created
     */
    @PostMapping
    public ResponseEntity<PlayerResponse> createPlayer(@Valid @RequestBody CreatePlayerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(playerService.createPlayer(request));
    }

    /**
     * Retrieves a player and their current statistics.
     *
     * @param playerId the player identifier
     * @return a response containing the player with status 200 OK
     * @throws com.svi.tictactoe.exception.ResourceNotFoundException if the player does not exist
     */
    @GetMapping("/{playerId}")
    public ResponseEntity<PlayerResponse> getPlayer(@PathVariable UUID playerId) {
        return ResponseEntity.ok(playerService.getPlayer(playerId));
    }
}
