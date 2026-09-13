package com.svi.tictactoe.event;

import com.svi.tictactoe.dto.response.room.RoomResponse;

public record RoomChangedEvent(
        RoomResponse room
) {
}