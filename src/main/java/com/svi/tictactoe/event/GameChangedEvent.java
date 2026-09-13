package com.svi.tictactoe.event;

import com.svi.tictactoe.dto.response.game.GameResponse;

public record GameChangedEvent(
        GameResponse game
) {
}