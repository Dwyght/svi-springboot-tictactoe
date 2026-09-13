package com.svi.tictactoe.mapper.persistence;

import com.svi.tictactoe.domain.Player;
import com.svi.tictactoe.entity.PlayerEntity;
import org.springframework.stereotype.Component;

@Component
public class PlayerPersistenceMapper {

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

    public Player toDomain(PlayerEntity entity) {
        Player player = new Player(entity.getPlayerId(), entity.getName());

        player.setWins(entity.getWins());
        player.setLosses(entity.getLosses());
        player.setDraws(entity.getDraws());
        player.setGamesPlayed(entity.getGamesPlayed());

        return player;
    }
}