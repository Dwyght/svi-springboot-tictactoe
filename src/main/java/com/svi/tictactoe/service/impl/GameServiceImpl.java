package com.svi.tictactoe.service.impl;

import com.svi.tictactoe.domain.Game;
import com.svi.tictactoe.domain.Player;
import com.svi.tictactoe.domain.Room;
import com.svi.tictactoe.dto.request.game.MakeMoveRequest;
import com.svi.tictactoe.dto.response.game.GameResponse;
import com.svi.tictactoe.engine.GameEngine;
import com.svi.tictactoe.entity.GameEntity;
import com.svi.tictactoe.entity.PlayerEntity;
import com.svi.tictactoe.entity.RoomEntity;
import com.svi.tictactoe.enums.ErrorMessage;
import com.svi.tictactoe.enums.GameResult;
import com.svi.tictactoe.enums.GameStatus;
import com.svi.tictactoe.enums.RoomStatus;
import com.svi.tictactoe.enums.Symbol;
import com.svi.tictactoe.exception.IllegalGameStateException;
import com.svi.tictactoe.exception.ResourceNotFoundException;
import com.svi.tictactoe.mapper.GameMapper;
import com.svi.tictactoe.mapper.persistence.GamePersistenceMapper;
import com.svi.tictactoe.mapper.persistence.PlayerPersistenceMapper;
import com.svi.tictactoe.mapper.persistence.RoomPersistenceMapper;
import com.svi.tictactoe.repository.GameRepository;
import com.svi.tictactoe.repository.PlayerRepository;
import com.svi.tictactoe.repository.RoomRepository;
import com.svi.tictactoe.service.GameService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final RoomRepository roomRepository;
    private final GameEngine gameEngine;
    private final GameMapper gameMapper;
    private final GamePersistenceMapper gamePersistenceMapper;
    private final PlayerPersistenceMapper playerPersistenceMapper;
    private final RoomPersistenceMapper roomPersistenceMapper;

    private final ConcurrentMap<UUID, Object> gameLocks = new ConcurrentHashMap<>();

    public GameServiceImpl(
            GameRepository gameRepository,
            PlayerRepository playerRepository,
            RoomRepository roomRepository,
            GameEngine gameEngine,
            GameMapper gameMapper,
            GamePersistenceMapper gamePersistenceMapper,
            PlayerPersistenceMapper playerPersistenceMapper,
            RoomPersistenceMapper roomPersistenceMapper
    ) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.roomRepository = roomRepository;
        this.gameEngine = gameEngine;
        this.gameMapper = gameMapper;
        this.gamePersistenceMapper = gamePersistenceMapper;
        this.playerPersistenceMapper = playerPersistenceMapper;
        this.roomPersistenceMapper = roomPersistenceMapper;
    }

    @Override
    public GameResponse getGame(UUID gameId) {
        Game game = findGame(gameId);
        return gameMapper.toResponse(game);
    }

    @Override
    public GameResponse makeMove(UUID gameId, MakeMoveRequest request) {
        Object lock = gameLocks.computeIfAbsent(gameId, ignored -> new Object());
        synchronized (lock) {return processMove(gameId, request);
        }
    }

    private GameResponse processMove(UUID gameId, MakeMoveRequest request) {
        Game game = findGame(gameId);

        validateGameInProgress(game);
        Symbol playerSymbol = getPlayerSymbol(game, request.playerId());
        validateTurn(game, request.playerId(), playerSymbol);
        gameEngine.placeSymbol(game.getBoard(), request.row(), request.column(), playerSymbol);
        game.setMoveCount(game.getMoveCount() + 1);
        game.setUpdatedAt(Instant.now());

        if (gameEngine.hasWinner(game.getBoard(), playerSymbol)) {
            finishWithWinner(game, request.playerId());
        } else if (gameEngine.isDraw(game.getBoard())) {
            finishWithDraw(game);
        } else {
            game.setNextTurn(gameEngine.getNextSymbol(playerSymbol));
        }

        GameEntity savedEntity = gameRepository.save(gamePersistenceMapper.toEntity(game));
        Game savedGame = gamePersistenceMapper.toDomain(savedEntity);
        return gameMapper.toResponse(savedGame);
    }

    private Game findGame(UUID gameId) {
        GameEntity entity = gameRepository.findById(gameId).orElseThrow(() ->
                new ResourceNotFoundException(ErrorMessage.GAME_NOT_FOUND.format(gameId)));
        return gamePersistenceMapper.toDomain(entity);
    }

    private void validateGameInProgress(Game game) {
        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            throw new IllegalGameStateException(ErrorMessage.GAME_NOT_IN_PROGRESS.format(game.getGameId()));
        }
    }

    private Symbol getPlayerSymbol(Game game, UUID playerId) {
        if (game.getPlayerXId().equals(playerId)) {
            return Symbol.X;
        }
        if (game.getPlayerOId().equals(playerId)) {
            return Symbol.O;
        }
        throw new IllegalGameStateException(ErrorMessage.PLAYER_NOT_IN_GAME.format(playerId, game.getGameId()));
    }

    private void validateTurn(Game game, UUID playerId, Symbol playerSymbol) {
        if (game.getNextTurn() != playerSymbol) {
            throw new IllegalGameStateException(ErrorMessage.NOT_PLAYER_TURN.format(playerId));
        }
    }

    private void finishWithWinner(Game game, UUID winnerId) {
        game.setStatus(GameStatus.FINISHED);
        game.setResult(GameResult.WIN);
        game.setWinnerId(winnerId);
        game.setNextTurn(null);
        game.setEndedAt(Instant.now());
        updateWinLossStatistics(game, winnerId);
        updateRoomAfterFinishedGame(game.getRoomCode());
    }

    private void finishWithDraw(Game game) {
        game.setStatus(GameStatus.FINISHED);
        game.setResult(GameResult.DRAW);
        game.setWinnerId(null);
        game.setNextTurn(null);
        game.setEndedAt(Instant.now());
        updateDrawStatistics(game);
        updateRoomAfterFinishedGame(game.getRoomCode());
    }

    private void updateWinLossStatistics(Game game, UUID winnerId) {
        Player playerX = findPlayer(game.getPlayerXId());
        Player playerO = findPlayer(game.getPlayerOId());

        Player winner;
        Player loser;
        if (playerX.getPlayerId().equals(winnerId)) {
            winner = playerX;
            loser = playerO;
        } else {
            winner = playerO;
            loser = playerX;
        }
        winner.setWins(winner.getWins() + 1);
        winner.setGamesPlayed(winner.getGamesPlayed() + 1);
        loser.setLosses(loser.getLosses() + 1);
        loser.setGamesPlayed(loser.getGamesPlayed() + 1);
        savePlayer(winner);
        savePlayer(loser);
    }

    private void updateDrawStatistics(Game game) {
        Player playerX = findPlayer(game.getPlayerXId());
        Player playerO = findPlayer(game.getPlayerOId());
        playerX.setDraws(playerX.getDraws() + 1);
        playerX.setGamesPlayed(playerX.getGamesPlayed() + 1);
        playerO.setDraws(playerO.getDraws() + 1);
        playerO.setGamesPlayed(playerO.getGamesPlayed() + 1);

        savePlayer(playerX);
        savePlayer(playerO);
    }

    private Player findPlayer(UUID playerId) {

        PlayerEntity entity = playerRepository.findById(playerId).orElseThrow(() ->
                        new ResourceNotFoundException(ErrorMessage.PLAYER_NOT_FOUND.format(playerId)));
        return playerPersistenceMapper.toDomain(entity);
    }

    private void savePlayer(Player player) {
        playerRepository.save(playerPersistenceMapper.toEntity(player));
    }

    private void updateRoomAfterFinishedGame(String roomCode) {
        RoomEntity entity = roomRepository.findById(roomCode).orElseThrow(() ->
                        new ResourceNotFoundException(ErrorMessage.ROOM_NOT_FOUND.format(roomCode)));

        Room room = roomPersistenceMapper.toDomain(entity);
        room.setStatus(RoomStatus.GAME_FINISHED);
        room.setUpdatedAt(Instant.now());
        roomRepository.save(roomPersistenceMapper.toEntity(room));
    }
}