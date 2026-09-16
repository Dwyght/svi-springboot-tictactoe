package com.svi.tictactoe.mapper;

import com.svi.tictactoe.dto.request.player.CreatePlayerRequest;
import com.svi.tictactoe.dto.response.player.PlayerResponse;
import com.svi.tictactoe.domain.Player;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Maps player requests and domain objects across the API boundary.
 */
@Component
public class PlayerMapper {

    /**
     * Creates a player domain object from a registration request and generated identifier.
     *
     * @param request the player registration details
     * @param playerId the generated player identifier
     * @return the player domain object
     */
    public Player toDomain(CreatePlayerRequest request, UUID playerId) {
        return new Player(playerId, request.name());
    }

    /**
     * Creates an API response from a player domain object.
     *
     * @param player the player domain object
     * @return the player response
     */
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
