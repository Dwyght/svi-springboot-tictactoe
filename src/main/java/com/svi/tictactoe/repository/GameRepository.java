package com.svi.tictactoe.repository;

import com.svi.tictactoe.entity.GameEntity;

import java.util.Optional;
import java.util.UUID;

public interface GameRepository {
    GameEntity save(GameEntity game);
    Optional<GameEntity> findById(UUID gameId);
}