package com.svi.tictactoe.repository;

import com.svi.tictactoe.entity.Room;

import java.util.Optional;

public interface RoomRepository {
    boolean createIfAbsent(Room room);
    Room save(Room room);
    Optional<Room> findByCode(String roomCode);
}