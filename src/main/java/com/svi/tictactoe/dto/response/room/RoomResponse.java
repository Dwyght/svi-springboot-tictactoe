package com.svi.tictactoe.dto.response.room;

import com.svi.tictactoe.enums.RoomStatus;
import java.time.Instant;
import java.util.UUID;

public record RoomResponse(
        String roomCode,
        UUID ownerPlayerId,
        UUID guestPlayerId,
        UUID currentGameId,
        RoomStatus status,
        Instant createdAt,
        Instant updatedAt
        ) {
}