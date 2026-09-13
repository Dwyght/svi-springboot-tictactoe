package com.svi.tictactoe.service;

import com.svi.tictactoe.dto.request.player.CreatePlayerRequest;
import com.svi.tictactoe.dto.response.player.PlayerResponse;

import java.util.UUID;

public interface PlayerService {

    PlayerResponse createPlayer(CreatePlayerRequest request);
    PlayerResponse getPlayer(UUID playerId);
}