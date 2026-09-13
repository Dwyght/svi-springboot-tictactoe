package com.svi.tictactoe.repository;

import java.util.Optional;
import java.util.UUID;

public interface ActiveRoomRepository {
    boolean assignIfAbsent(UUID playerId, String roomCode);
    Optional<String> findRoomCodeByPlayerId(UUID playerId);
    void remove(UUID playerId);
}