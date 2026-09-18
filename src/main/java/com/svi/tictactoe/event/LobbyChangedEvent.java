package com.svi.tictactoe.event;

import com.svi.tictactoe.dto.response.room.LobbyResponse;

public record LobbyChangedEvent(
        LobbyResponse lobby
) {
}
