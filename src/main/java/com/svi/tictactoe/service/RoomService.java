package com.svi.tictactoe.service;

import com.svi.tictactoe.dto.request.room.CreateRoomRequest;
import com.svi.tictactoe.dto.request.room.JoinRoomRequest;
import com.svi.tictactoe.dto.request.game.CreateGameRequest;
import com.svi.tictactoe.dto.response.room.RoomResponse;

import java.util.UUID;

public interface RoomService {

    RoomResponse createRoom(CreateRoomRequest request);
    RoomResponse getRoom(String roomCode);
    RoomResponse joinRoom(String roomCode, JoinRoomRequest request);
    RoomResponse leaveRoom(String roomCode, UUID playerId);
    RoomResponse createGame(String roomCode, CreateGameRequest request);
}
