package com.svi.tictactoe.engine;

import com.svi.tictactoe.constant.GameConstants;
import com.svi.tictactoe.enums.CellValue;
import com.svi.tictactoe.enums.Symbol;
import com.svi.tictactoe.exception.InvalidMoveException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameEngineTest {

    private final GameEngine gameEngine = new GameEngine();

    @Nested
    class PlaceSymbolTests {

        @Test
        void placesXInEmptyCell() {
            // Arrange
            CellValue[][] board = emptyBoard();

            // Act
            gameEngine.placeSymbol(board, 1, 2, Symbol.X);

            // Assert
            assertEquals(CellValue.X, board[1][2]);
        }

        @Test
        void placesOInEmptyCell() {
            // Arrange
            CellValue[][] board = emptyBoard();

            // Act
            gameEngine.placeSymbol(board, 2, 1, Symbol.O);

            // Assert
            assertEquals(CellValue.O, board[2][1]);
        }

        @Test
        void rejectsAlreadyOccupiedCell() {
            // Arrange
            CellValue[][] board = emptyBoard();
            board[1][1] = CellValue.X;

            // Act and Assert
            assertThrows(
                    InvalidMoveException.class,
                    () -> gameEngine.placeSymbol(board, 1, 1, Symbol.O)
            );
            assertEquals(CellValue.X, board[1][1]);
        }

        @Test
        void rejectsRowBelowZero() {
            // Arrange
            CellValue[][] board = emptyBoard();

            // Act and Assert
            assertThrows(
                    InvalidMoveException.class,
                    () -> gameEngine.placeSymbol(board, -1, 0, Symbol.X)
            );
        }

        @Test
        void rejectsRowAtBoardSize() {
            // Arrange
            CellValue[][] board = emptyBoard();

            // Act and Assert
            assertThrows(
                    InvalidMoveException.class,
                    () -> gameEngine.placeSymbol(board, GameConstants.BOARD_SIZE, 0, Symbol.X)
            );
        }

        @Test
        void rejectsColumnBelowZero() {
            // Arrange
            CellValue[][] board = emptyBoard();

            // Act and Assert
            assertThrows(
                    InvalidMoveException.class,
                    () -> gameEngine.placeSymbol(board, 0, -1, Symbol.X)
            );
        }

        @Test
        void rejectsColumnAtBoardSize() {
            // Arrange
            CellValue[][] board = emptyBoard();

            // Act and Assert
            assertThrows(
                    InvalidMoveException.class,
                    () -> gameEngine.placeSymbol(board, 0, GameConstants.BOARD_SIZE, Symbol.X)
            );
        }
    }

    @Nested
    class HasWinnerTests {

        @Test
        void detectsHorizontalWin() {
            // Arrange
            CellValue[][] board = emptyBoard();
            Arrays.fill(board[1], CellValue.X);

            // Act
            boolean hasWinner = gameEngine.hasWinner(board, Symbol.X);

            // Assert
            assertTrue(hasWinner);
        }

        @Test
        void detectsVerticalWin() {
            // Arrange
            CellValue[][] board = emptyBoard();
            for (int row = 0; row < GameConstants.BOARD_SIZE; row++) {
                board[row][2] = CellValue.O;
            }

            // Act
            boolean hasWinner = gameEngine.hasWinner(board, Symbol.O);

            // Assert
            assertTrue(hasWinner);
        }

        @Test
        void detectsMainDiagonalWin() {
            // Arrange
            CellValue[][] board = emptyBoard();
            for (int index = 0; index < GameConstants.BOARD_SIZE; index++) {
                board[index][index] = CellValue.X;
            }

            // Act
            boolean hasWinner = gameEngine.hasWinner(board, Symbol.X);

            // Assert
            assertTrue(hasWinner);
        }

        @Test
        void detectsAntiDiagonalWin() {
            // Arrange
            CellValue[][] board = emptyBoard();
            for (int index = 0; index < GameConstants.BOARD_SIZE; index++) {
                board[index][GameConstants.BOARD_SIZE - 1 - index] = CellValue.O;
            }

            // Act
            boolean hasWinner = gameEngine.hasWinner(board, Symbol.O);

            // Assert
            assertTrue(hasWinner);
        }

        @Test
        void returnsFalseWhenNoWinningLineExists() {
            // Arrange
            CellValue[][] board = emptyBoard();
            board[0][0] = CellValue.X;
            board[0][1] = CellValue.O;
            board[1][1] = CellValue.X;
            board[2][0] = CellValue.O;

            // Act
            boolean hasWinner = gameEngine.hasWinner(board, Symbol.X);

            // Assert
            assertFalse(hasWinner);
        }

        @Test
        void evaluatesXAndOIndependently() {
            // Arrange
            CellValue[][] board = emptyBoard();
            Arrays.fill(board[0], CellValue.X);
            board[1][0] = CellValue.O;
            board[1][1] = CellValue.O;

            // Act
            boolean xHasWinner = gameEngine.hasWinner(board, Symbol.X);
            boolean oHasWinner = gameEngine.hasWinner(board, Symbol.O);

            // Assert
            assertAll(
                    () -> assertTrue(xHasWinner),
                    () -> assertFalse(oHasWinner)
            );
        }
    }

    @Nested
    class IsDrawTests {

        @Test
        void returnsTrueForCompletelyFullBoard() {
            // Arrange
            CellValue[][] board = {
                    {CellValue.X, CellValue.X, CellValue.X},
                    {CellValue.O, CellValue.O, CellValue.X},
                    {CellValue.O, CellValue.X, CellValue.O}
            };

            // Act
            boolean draw = gameEngine.isDraw(board);

            // Assert
            assertTrue(draw);
        }

        @Test
        void returnsFalseWhenAtLeastOneCellIsEmpty() {
            // Arrange
            CellValue[][] board = {
                    {CellValue.X, CellValue.O, CellValue.X},
                    {CellValue.O, CellValue.EMPTY, CellValue.O},
                    {CellValue.O, CellValue.X, CellValue.X}
            };

            // Act
            boolean draw = gameEngine.isDraw(board);

            // Assert
            assertFalse(draw);
        }
    }

    @Nested
    class GetNextSymbolTests {

        @Test
        void returnsOAfterX() {
            // Act
            Symbol nextSymbol = gameEngine.getNextSymbol(Symbol.X);

            // Assert
            assertEquals(Symbol.O, nextSymbol);
        }

        @Test
        void returnsXAfterO() {
            // Act
            Symbol nextSymbol = gameEngine.getNextSymbol(Symbol.O);

            // Assert
            assertEquals(Symbol.X, nextSymbol);
        }
    }

    @Nested
    class IsCellEmptyTests {

        @Test
        void returnsTrueForEmptyCell() {
            // Arrange
            CellValue[][] board = emptyBoard();

            // Act
            boolean empty = gameEngine.isCellEmpty(board, 0, 0);

            // Assert
            assertTrue(empty);
        }

        @Test
        void returnsFalseForXCell() {
            // Arrange
            CellValue[][] board = emptyBoard();
            board[0][0] = CellValue.X;

            // Act
            boolean empty = gameEngine.isCellEmpty(board, 0, 0);

            // Assert
            assertFalse(empty);
        }

        @Test
        void returnsFalseForOCell() {
            // Arrange
            CellValue[][] board = emptyBoard();
            board[0][0] = CellValue.O;

            // Act
            boolean empty = gameEngine.isCellEmpty(board, 0, 0);

            // Assert
            assertFalse(empty);
        }
    }

    private CellValue[][] emptyBoard() {
        CellValue[][] board = new CellValue[GameConstants.BOARD_SIZE][GameConstants.BOARD_SIZE];
        for (CellValue[] row : board) {
            Arrays.fill(row, CellValue.EMPTY);
        }
        return board;
    }
}
