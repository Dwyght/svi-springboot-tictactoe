package com.svi.tictactoe.controller;

import com.svi.tictactoe.dto.request.game.MakeMoveRequest;
import com.svi.tictactoe.dto.response.game.GameResponse;
import com.svi.tictactoe.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Exposes REST operations for retrieving games and submitting player moves.
 */
@RestController
@RequestMapping("/api/v1/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    /**
     * Retrieves the current state of a game.
     *
     * @param gameId the game identifier
     * @return a response containing the game with status 200 OK
     * @throws com.svi.tictactoe.exception.ResourceNotFoundException if the game does not exist
     */
    @GetMapping("/{gameId}")
    public ResponseEntity<GameResponse> getGame(@PathVariable UUID gameId) {
        return ResponseEntity.ok(gameService.getGame(gameId));
    }

    /**
     * Submits a validated move for an active game.
     *
     * @param gameId the game identifier
     * @param request the player and board position for the move
     * @return a response containing the updated game with status 200 OK
     * @throws com.svi.tictactoe.exception.ResourceNotFoundException if the game does not exist
     * @throws com.svi.tictactoe.exception.IllegalGameStateException if the game or player is not eligible for the move
     * @throws com.svi.tictactoe.exception.InvalidMoveException if the selected board position is invalid or occupied
     */
    @PostMapping("/{gameId}/moves")
    public ResponseEntity<GameResponse> makeMove(@PathVariable UUID gameId, @Valid @RequestBody MakeMoveRequest request) {
        return ResponseEntity.ok(gameService.makeMove(gameId, request));
    }
}
