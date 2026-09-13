package com.svi.tictactoe.repository;

import com.svi.tictactoe.entity.Player;

import java.util.Optional;
import java.util.UUID;

public interface PlayerRepository {
    Player save(Player player);
    Optional<Player> findById(UUID playerId);
    boolean existsById(UUID playerId);
}