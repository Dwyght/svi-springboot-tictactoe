package com.svi.tictactoe.dto.response.game;

import com.svi.tictactoe.enums.CellValue;
import com.svi.tictactoe.enums.GameResult;
import com.svi.tictactoe.enums.GameStatus;
import com.svi.tictactoe.enums.Symbol;
import java.time.Instant;
import java.util.UUID;

public record GameResponse(
        UUID gameId,
        String roomCode,
        UUID playerXId,
        UUID playerOId,
        CellValue[][] board,
        Symbol nextTurn,
        GameStatus status,
        GameResult result,
        UUID winnerId,
        int moveCount,
        Instant createdAt,
        Instant updatedAt,
        Instant endedAt
        ) {
}