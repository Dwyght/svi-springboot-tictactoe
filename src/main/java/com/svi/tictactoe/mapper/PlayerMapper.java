package com.svi.tictactoe.mapper;

import com.svi.tictactoe.dto.request.player.CreatePlayerRequest;
import com.svi.tictactoe.dto.response.player.PlayerResponse;
import com.svi.tictactoe.domain.Player;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PlayerMapper {

    public Player toDomain(CreatePlayerRequest request, UUID playerId) {
        return new Player(playerId, request.name());
    }

    public PlayerResponse toResponse(Player player) {
        return new PlayerResponse(
                player.getPlayerId(),
                player.getName(),
                player.getWins(),
                player.getLosses(),
                player.getDraws(),
                player.getGamesPlayed()
        );
    }
}