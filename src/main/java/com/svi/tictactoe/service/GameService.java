package com.svi.tictactoe.service;

import com.svi.tictactoe.dto.request.game.MakeMoveRequest;
import com.svi.tictactoe.dto.response.game.GameResponse;

import java.util.UUID;

public interface GameService {

    GameResponse getGame(UUID gameId);
    GameResponse makeMove(UUID gameId, MakeMoveRequest request);
}