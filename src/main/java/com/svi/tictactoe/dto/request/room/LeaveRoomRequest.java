package com.svi.tictactoe.dto.request.room;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record LeaveRoomRequest(
        @NotNull
        UUID playerId
        ) {
}