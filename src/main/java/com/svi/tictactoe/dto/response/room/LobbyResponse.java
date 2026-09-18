package com.svi.tictactoe.dto.response.room;

import java.util.List;

public record LobbyResponse(
        List<RoomResponse> rooms
) {
}
