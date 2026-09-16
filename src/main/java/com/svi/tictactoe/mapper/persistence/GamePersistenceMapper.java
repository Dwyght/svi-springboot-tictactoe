package com.svi.tictactoe.mapper.persistence;

import com.svi.tictactoe.constant.GameConstants;
import com.svi.tictactoe.domain.Game;
import com.svi.tictactoe.entity.GameEntity;
import com.svi.tictactoe.enums.CellValue;
import com.svi.tictactoe.enums.GameResult;
import com.svi.tictactoe.enums.GameStatus;
import com.svi.tictactoe.enums.Symbol;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts games between domain and Cassandra persistence representations.
 */
@Component
public class GamePersistenceMapper {

    /**
     * Converts a game domain object into a Cassandra entity with a flattened board.
     *
     * @param game the game domain object
     * @return the game entity
     */
    public GameEntity toEntity(Game game) {
        GameEntity entity = new GameEntity();

        entity.setGameId(game.getGameId());
        entity.setRoomCode(game.getRoomCode());
        entity.setPlayerXId(game.getPlayerXId());
        entity.setPlayerOId(game.getPlayerOId());

        entity.setBoard(flattenBoard(game.getBoard()));
        entity.setNextTurn(game.getNextTurn() == null ? null : game.getNextTurn().name());
        entity.setStatus(game.getStatus() == null ? null : game.getStatus().name());
        entity.setResult(game.getResult() == null ? null : game.getResult().name());

        entity.setWinnerId(game.getWinnerId());
        entity.setMoveCount(game.getMoveCount());
        entity.setCreatedAt(game.getCreatedAt());
        entity.setUpdatedAt(game.getUpdatedAt());
        entity.setEndedAt(game.getEndedAt());

        return entity;
    }

    /**
     * Converts a Cassandra game entity into a domain object with a two-dimensional board.
     *
     * @param entity the game entity
     * @return the game domain object
     */
    public Game toDomain(GameEntity entity) {
        Game game = new Game();

        game.setGameId(entity.getGameId());
        game.setRoomCode(entity.getRoomCode());
        game.setPlayerXId(entity.getPlayerXId());
        game.setPlayerOId(entity.getPlayerOId());

        game.setBoard(expandBoard(entity.getBoard()));
        game.setNextTurn(entity.getNextTurn() == null ? null : Symbol.valueOf(entity.getNextTurn()));
        game.setStatus(entity.getStatus() == null ? null : GameStatus.valueOf(entity.getStatus()));
        game.setResult(entity.getResult() == null ? null : GameResult.valueOf(entity.getResult()));

        game.setWinnerId(entity.getWinnerId());
        game.setMoveCount(entity.getMoveCount());
        game.setCreatedAt(entity.getCreatedAt());
        game.setUpdatedAt(entity.getUpdatedAt());
        game.setEndedAt(entity.getEndedAt());

        return game;
    }

    private List<String> flattenBoard(CellValue[][] board) {
        List<String> flattenedBoard = new ArrayList<>();

        for (int row = 0; row < GameConstants.BOARD_SIZE; row++) {
            for (int column = 0;
                 column < GameConstants.BOARD_SIZE;
                 column++) {
                flattenedBoard.add(board[row][column].name());
            }
        }

        return flattenedBoard;
    }

    private CellValue[][] expandBoard(List<String> board) {
        CellValue[][] expandedBoard = new CellValue[GameConstants.BOARD_SIZE][GameConstants.BOARD_SIZE];

        for (int index = 0; index < board.size(); index++) {
            int row = index / GameConstants.BOARD_SIZE;
            int column = index % GameConstants.BOARD_SIZE;
            expandedBoard[row][column] = CellValue.valueOf(board.get(index));
        }

        return expandedBoard;
    }
}
