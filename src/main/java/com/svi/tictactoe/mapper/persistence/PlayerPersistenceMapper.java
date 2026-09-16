package com.svi.tictactoe.mapper.persistence;

import com.svi.tictactoe.domain.Player;
import com.svi.tictactoe.entity.PlayerEntity;
import org.springframework.stereotype.Component;

/**
 * Converts players between domain and Cassandra persistence representations.
 */
@Component
public class PlayerPersistenceMapper {

    /**
     * Converts a player domain object into a Cassandra entity.
     *
     * @param player the player domain object
     * @return the player entity
     */
    public PlayerEntity toEntity(Player player) {
        PlayerEntity entity = new PlayerEntity();

        entity.setPlayerId(player.getPlayerId());
        entity.setName(player.getName());
        entity.setWins(player.getWins());
        entity.setLosses(player.getLosses());
        entity.setDraws(player.getDraws());
        entity.setGamesPlayed(player.getGamesPlayed());

        return entity;
    }

    /**
     * Converts a Cassandra player entity into a domain object.
     *
     * @param entity the player entity
     * @return the player domain object
     */
    public Player toDomain(PlayerEntity entity) {
        Player player = new Player(entity.getPlayerId(), entity.getName());

        player.setWins(entity.getWins());
        player.setLosses(entity.getLosses());
        player.setDraws(entity.getDraws());
        player.setGamesPlayed(entity.getGamesPlayed());

        return player;
    }
}
