package com.svi.tictactoe.service.impl;

import com.svi.tictactoe.domain.Game;
import com.svi.tictactoe.domain.Room;
import com.svi.tictactoe.dto.request.game.MakeMoveRequest;
import com.svi.tictactoe.dto.response.game.GameResponse;
import com.svi.tictactoe.dto.response.room.RoomResponse;
import com.svi.tictactoe.engine.GameEngine;
import com.svi.tictactoe.entity.GameEntity;
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
import com.svi.tictactoe.mapper.persistence.RoomPersistenceMapper;
import com.svi.tictactoe.repository.GameRepository;
import com.svi.tictactoe.repository.RoomRepository;
import com.svi.tictactoe.service.GameService;
import com.svi.tictactoe.service.PlayerStatisticsService;
import com.svi.tictactoe.event.GameChangedEvent;
import com.svi.tictactoe.event.RoomChangedEvent;
import com.svi.tictactoe.mapper.RoomMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final RoomRepository roomRepository;
    private final GameEngine gameEngine;
    private final GameMapper gameMapper;
    private final GamePersistenceMapper gamePersistenceMapper;
    private final RoomPersistenceMapper roomPersistenceMapper;
    private final PlayerStatisticsService playerStatisticsService;
    private final RoomMapper roomMapper;
    private final ApplicationEventPublisher eventPublisher;

    private final ConcurrentMap<UUID, Object> gameLocks = new ConcurrentHashMap<>();

    public GameServiceImpl(
            GameRepository gameRepository,
            RoomRepository roomRepository,
            GameEngine gameEngine,
            GameMapper gameMapper,
            GamePersistenceMapper gamePersistenceMapper,
            RoomPersistenceMapper roomPersistenceMapper,
            PlayerStatisticsService playerStatisticsService,
            RoomMapper roomMapper,
            ApplicationEventPublisher eventPublisher
    ) {
        this.gameRepository = gameRepository;
        this.roomRepository = roomRepository;
        this.gameEngine = gameEngine;
        this.gameMapper = gameMapper;
        this.gamePersistenceMapper = gamePersistenceMapper;
        this.roomPersistenceMapper = roomPersistenceMapper;
        this.playerStatisticsService = playerStatisticsService;
        this.roomMapper = roomMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public GameResponse getGame(UUID gameId) {
        Game game = findGame(gameId);
        return gameMapper.toResponse(game);
    }

    @Override
    public GameResponse makeMove(UUID gameId, MakeMoveRequest request) {
        Object lock = gameLocks.computeIfAbsent(gameId, ignored -> new Object());
        synchronized (lock) {
            return processMove(gameId, request);
        }
    }

    @Override
    public void forfeitGame(UUID gameId, UUID forfeitingPlayerId) {
        Object lock = gameLocks.computeIfAbsent(gameId, ignored -> new Object());
        synchronized (lock) {
            Game game = findGame(gameId);
            if (game.getStatus() != GameStatus.IN_PROGRESS) {
                return;
            }
            getPlayerSymbol(game, forfeitingPlayerId);
            UUID winnerId = game.getPlayerXId().equals(forfeitingPlayerId) ? game.getPlayerOId() : game.getPlayerXId();
            finishWithForfeit(game, winnerId);
            GameEntity savedEntity = gameRepository.save(gamePersistenceMapper.toEntity(game));
            publishGameChanged(savedEntity);
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
        return publishGameChanged(savedEntity);
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
        UUID loserId = game.getPlayerXId().equals(winnerId) ? game.getPlayerOId() : game.getPlayerXId();
        playerStatisticsService.recordWinLoss(winnerId, loserId);
        updateRoomAfterFinishedGame(game.getRoomCode());
    }

    private void finishWithDraw(Game game) {
        game.setStatus(GameStatus.FINISHED);
        game.setResult(GameResult.DRAW);
        game.setWinnerId(null);
        game.setNextTurn(null);
        game.setEndedAt(Instant.now());
        playerStatisticsService.recordDraw(game.getPlayerXId(), game.getPlayerOId());
        updateRoomAfterFinishedGame(game.getRoomCode());
    }

    private void updateRoomAfterFinishedGame(String roomCode) {
        RoomEntity entity = roomRepository
                .findById(roomCode)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.ROOM_NOT_FOUND.format(roomCode)));

        Room room = roomPersistenceMapper.toDomain(entity);
        room.setStatus(RoomStatus.GAME_FINISHED);
        room.setUpdatedAt(Instant.now());
        RoomEntity savedEntity = roomRepository.save(roomPersistenceMapper.toEntity(room));
        Room savedRoom = roomPersistenceMapper.toDomain(savedEntity);
        RoomResponse response = roomMapper.toResponse(savedRoom);
        eventPublisher.publishEvent(new RoomChangedEvent(response));
    }

    private void finishWithForfeit(Game game, UUID winnerId) {
        game.setStatus(GameStatus.FINISHED);
        game.setResult(GameResult.FORFEIT);
        game.setWinnerId(winnerId);
        game.setNextTurn(null);
        game.setUpdatedAt(Instant.now());
        game.setEndedAt(Instant.now());
        UUID loserId = game.getPlayerXId().equals(winnerId) ? game.getPlayerOId() : game.getPlayerXId();
        playerStatisticsService.recordWinLoss(winnerId, loserId);
    }

    private GameResponse publishGameChanged(GameEntity entity) {
        Game game = gamePersistenceMapper.toDomain(entity);
        GameResponse response = gameMapper.toResponse(game);
        eventPublisher.publishEvent(new GameChangedEvent(response));
        return response;
    }
}
