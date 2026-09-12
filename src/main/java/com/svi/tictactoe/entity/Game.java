package com.svi.tictactoe.entity;

import com.svi.tictactoe.constant.GameConstants;
import com.svi.tictactoe.enums.CellValue;
import com.svi.tictactoe.enums.GameResult;
import com.svi.tictactoe.enums.GameStatus;
import com.svi.tictactoe.enums.Symbol;

import java.time.Instant;
import java.util.UUID;

public class Game {

    private UUID gameId;
    private String roomCode;
    private UUID playerXId;
    private UUID playerOId;
    private CellValue[][] board;
    private Symbol nextTurn;
    private GameStatus status;
    private GameResult result;
    private UUID winnerId;
    private int moveCount;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant endedAt;

    public Game() {
    }

    public Game(UUID gameId,String roomCode, UUID playerXId, UUID playerOId) {
        this.gameId = gameId;
        this.roomCode = roomCode;
        this.playerXId = playerXId;
        this.playerOId = playerOId;
        this.board = createEmptyBoard();
        this.nextTurn = Symbol.X;
        this.status = GameStatus.IN_PROGRESS;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    private CellValue[][] createEmptyBoard() {
        CellValue[][] board = new CellValue[GameConstants.BOARD_SIZE][GameConstants.BOARD_SIZE];

        for (int row = 0; row < GameConstants.BOARD_SIZE; row++) {
            for (int column = 0; column < GameConstants.BOARD_SIZE; column++) {
                board[row][column] = CellValue.EMPTY;
            }
        }
        return board;
    }

    public UUID getGameId() {
        return gameId;
    }

    public void setGameId(UUID gameId) {
        this.gameId = gameId;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public UUID getPlayerXId() {
        return playerXId;
    }

    public void setPlayerXId(UUID playerXId) {
        this.playerXId = playerXId;
    }

    public UUID getPlayerOId() {
        return playerOId;
    }

    public void setPlayerOId(UUID playerOId) {
        this.playerOId = playerOId;
    }

    public CellValue[][] getBoard() {
        return board;
    }

    public void setBoard(CellValue[][] board) {
        this.board = board;
    }

    public Symbol getNextTurn() {
        return nextTurn;
    }

    public void setNextTurn(Symbol nextTurn) {
        this.nextTurn = nextTurn;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public GameResult getResult() {
        return result;
    }

    public void setResult(GameResult result) {
        this.result = result;
    }

    public UUID getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(UUID winnerId) {
        this.winnerId = winnerId;
    }

    public int getMoveCount() {
        return moveCount;
    }

    public void setMoveCount(int moveCount) {
        this.moveCount = moveCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Instant endedAt) {
        this.endedAt = endedAt;
    }
}
