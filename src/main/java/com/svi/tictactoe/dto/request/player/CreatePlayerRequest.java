package com.svi.tictactoe.dto.request.player;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePlayerRequest(
        @NotBlank
        @Size(max = 10)
        String name
        ) {
}