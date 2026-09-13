package com.svi.tictactoe.controller;

import com.svi.tictactoe.dto.request.game.MakeMoveRequest;
import com.svi.tictactoe.dto.response.game.GameResponse;
import com.svi.tictactoe.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameResponse> getGame(@PathVariable UUID gameId) {
        return ResponseEntity.ok(gameService.getGame(gameId));
    }

    @PostMapping("/{gameId}/moves")
    public ResponseEntity<GameResponse> makeMove(@PathVariable UUID gameId, @Valid @RequestBody MakeMoveRequest request) {
        return ResponseEntity.ok(gameService.makeMove(gameId, request));
    }
}