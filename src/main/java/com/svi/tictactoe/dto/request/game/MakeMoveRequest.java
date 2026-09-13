package com.svi.tictactoe.dto.request.game;

import com.svi.tictactoe.constant.GameConstants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record MakeMoveRequest(
        @NotNull
        UUID playerId,

        @NotNull
        @Min(0)
        @Max(GameConstants.BOARD_SIZE - 1)
        Integer row,

        @NotNull
        @Min(0)
        @Max(GameConstants.BOARD_SIZE - 1)
        Integer column
        ) {
}