package com.svi.tictactoe.engine;

import com.svi.tictactoe.constant.GameConstants;
import com.svi.tictactoe.enums.CellValue;
import com.svi.tictactoe.enums.ErrorMessage;
import com.svi.tictactoe.enums.Symbol;
import com.svi.tictactoe.exception.InvalidMoveException;
import org.springframework.stereotype.Component;

/**
 * Applies Tic-Tac-Toe board rules and evaluates move outcomes independently of persistence.
 */
@Component
public class GameEngine {

    /**
     * Places a player's symbol in an available board cell.
     *
     * @param board the game board
     * @param row the zero-based row index
     * @param column the zero-based column index
     * @param symbol the symbol to place
     * @throws InvalidMoveException if the position is outside the board or already occupied
     */
    public void placeSymbol(CellValue[][] board, int row, int column, Symbol symbol) {
        validatePosition(row, column);
        if (!isCellEmpty(board, row, column)) {
            throw new InvalidMoveException(ErrorMessage.CELL_ALREADY_OCCUPIED.getMessage());
        }
        board[row][column] = toCellValue(symbol);
    }

    /**
     * Determines whether a symbol occupies a complete row, column, or diagonal.
     *
     * @param board the game board
     * @param symbol the symbol to evaluate
     * @return {@code true} when the symbol has a winning line
     */
    public boolean hasWinner(CellValue[][] board, Symbol symbol) {
        CellValue value = toCellValue(symbol);
        return hasWinningRow(board, value) || hasWinningColumn(board, value) || hasWinningDiagonal(board, value);
    }

    /**
     * Determines whether the board has no empty cells remaining.
     *
     * @param board the game board
     * @return {@code true} when every cell is occupied
     */
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

    /**
     * Returns the symbol that plays after the current symbol.
     *
     * @param currentSymbol the symbol that just played
     * @return the opposing symbol
     */
    public Symbol getNextSymbol(Symbol currentSymbol) {
        return currentSymbol == Symbol.X ? Symbol.O : Symbol.X;
    }

    /**
     * Checks whether a board cell is available.
     *
     * @param board the game board
     * @param row the zero-based row index
     * @param column the zero-based column index
     * @return {@code true} when the cell is empty
     */
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
