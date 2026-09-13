package com.svi.tictactoe.dto.request.game;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateRematchGameRequest(
        @NotNull
        UUID playerId
        ) {
}