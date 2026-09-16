package com.svi.tictactoe.mapper;

import com.svi.tictactoe.dto.response.game.GameResponse;
import com.svi.tictactoe.domain.Game;
import com.svi.tictactoe.enums.CellValue;
import org.springframework.stereotype.Component;

/**
 * Maps game domain objects to API response models.
 */
@Component
public class GameMapper {

    /**
     * Creates an API response from a game while protecting its mutable board data.
     *
     * @param game the game domain object
     * @return the game response
     */
    public GameResponse toResponse(Game game) {
        return new GameResponse(
                game.getGameId(),
                game.getRoomCode(),
                game.getPlayerXId(),
                game.getPlayerOId(),
                copyBoard(game.getBoard()),
                game.getNextTurn(),
                game.getStatus(),
                game.getResult(),
                game.getWinnerId(),
                game.getMoveCount(),
                game.getCreatedAt(),
                game.getUpdatedAt(),
                game.getEndedAt()
        );
    }

    private CellValue[][] copyBoard(CellValue[][] board) {
        if (board == null) {
            return null;
        }
        CellValue[][] copy = new CellValue[board.length][];
        for (int row = 0; row < board.length; row++) {
            copy[row] = board[row].clone();
        }
        return copy;
    }
}
