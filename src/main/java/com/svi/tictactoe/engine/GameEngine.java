package com.svi.tictactoe.engine;

import com.svi.tictactoe.constant.GameConstants;
import com.svi.tictactoe.enums.CellValue;
import com.svi.tictactoe.enums.ErrorMessage;
import com.svi.tictactoe.enums.Symbol;
import com.svi.tictactoe.exception.InvalidMoveException;
import org.springframework.stereotype.Component;

@Component
public class GameEngine {

    public void placeSymbol(CellValue[][] board, int row, int column, Symbol symbol) {
        validatePosition(row, column);
        if (!isCellEmpty(board, row, column)) {
            throw new InvalidMoveException(ErrorMessage.CELL_ALREADY_OCCUPIED.getMessage());
        }
        board[row][column] = toCellValue(symbol);
    }

    public boolean hasWinner(CellValue[][] board, Symbol symbol) {
        CellValue value = toCellValue(symbol);
        return hasWinningRow(board, value) || hasWinningColumn(board, value) || hasWinningDiagonal(board, value);
    }

    public boolean isDraw(CellValue[][] board) {
        for (int row = 0; row < GameConstants.BOARD_SIZE; row++) {
            for (int column = 0; column < GameConstants.BOARD_SIZE; column++) {
                if (board[row][column] == CellValue.EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }

    public Symbol getNextSymbol(Symbol currentSymbol) {
        return currentSymbol == Symbol.X ? Symbol.O : Symbol.X;
    }

    public boolean isCellEmpty(CellValue[][] board, int row, int column) {
        return board[row][column] == CellValue.EMPTY;
    }

    private void validatePosition(int row, int column) {
        if (row < 0 || row >= GameConstants.BOARD_SIZE || column < 0 || column >= GameConstants.BOARD_SIZE) {
            throw new InvalidMoveException(ErrorMessage.INVALID_BOARD_POSITION.format(GameConstants.BOARD_SIZE - 1));
        }
    }

    private boolean hasWinningRow(CellValue[][] board, CellValue value) {
        for (int row = 0; row < GameConstants.BOARD_SIZE; row++) {
            boolean winningRow = true;
            for (int column = 0; column < GameConstants.BOARD_SIZE; column++) {
                if (board[row][column] != value) {
                    winningRow = false;
                    break;
                }
            }
            if (winningRow) {
                return true;
            }
        }
        return false;
    }

    private boolean hasWinningColumn(CellValue[][] board, CellValue value) {
        for (int column = 0; column < GameConstants.BOARD_SIZE; column++) {
            boolean winningColumn = true;
            for (int row = 0; row < GameConstants.BOARD_SIZE; row++) {
                if (board[row][column] != value) {
                    winningColumn = false;
                    break;
                }
            }
            if (winningColumn) {
                return true;
            }
        }
        return false;
    }

    private boolean hasWinningDiagonal(CellValue[][] board, CellValue value) {
        boolean primaryDiagonal = true;
        boolean secondaryDiagonal = true;
        for (int index = 0; index < GameConstants.BOARD_SIZE; index++) {
            if (board[index][index] != value) {
                primaryDiagonal = false;
            }
            if (board[index][GameConstants.BOARD_SIZE - 1 - index] != value) {
                secondaryDiagonal = false;
            }
        }
        return primaryDiagonal || secondaryDiagonal;
    }

    private CellValue toCellValue(Symbol symbol) {
        return switch (symbol) {
            case X -> CellValue.X;
            case O -> CellValue.O;
        };
    }
}