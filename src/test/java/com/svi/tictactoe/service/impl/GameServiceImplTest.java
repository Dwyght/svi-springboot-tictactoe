package com.svi.tictactoe.service.impl;

import com.svi.tictactoe.dto.request.game.MakeMoveRequest;
import com.svi.tictactoe.engine.GameEngine;
import com.svi.tictactoe.entity.GameEntity;
import com.svi.tictactoe.entity.RoomEntity;
import com.svi.tictactoe.enums.CellValue;
import com.svi.tictactoe.enums.GameResult;
import com.svi.tictactoe.enums.GameStatus;
import com.svi.tictactoe.enums.RoomStatus;
import com.svi.tictactoe.enums.Symbol;
import com.svi.tictactoe.event.GameChangedEvent;
import com.svi.tictactoe.event.RoomChangedEvent;
import com.svi.tictactoe.exception.IllegalGameStateException;
import com.svi.tictactoe.mapper.GameMapper;
import com.svi.tictactoe.mapper.RoomMapper;
import com.svi.tictactoe.mapper.persistence.GamePersistenceMapper;
import com.svi.tictactoe.mapper.persistence.RoomPersistenceMapper;
import com.svi.tictactoe.repository.GameRepository;
import com.svi.tictactoe.repository.RoomRepository;
import com.svi.tictactoe.service.PlayerStatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class GameServiceImplTest {

    private GameRepository gameRepository;
    private RoomRepository roomRepository;
    private GameEngine gameEngine;
    private PlayerStatisticsService playerStatisticsService;
    private ApplicationEventPublisher eventPublisher;
    private GameServiceImpl gameService;

    @BeforeEach
    void setUp() {
        gameRepository = mock(GameRepository.class);
        roomRepository = mock(RoomRepository.class);
        gameEngine = mock(GameEngine.class);
        playerStatisticsService = mock(PlayerStatisticsService.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        gameService = new GameServiceImpl(
                gameRepository,
                roomRepository,
                gameEngine,
                new GameMapper(),
                new GamePersistenceMapper(),
                new RoomPersistenceMapper(),
                playerStatisticsService,
                new RoomMapper(),
                eventPublisher
        );
    }

    @Test
    void makeMoveThrowsIllegalGameStateExceptionWhenItIsNotRequestingPlayersTurn() {
        // Arrange
        UUID gameId = UUID.randomUUID();
        UUID playerXId = UUID.randomUUID();
        UUID playerOId = UUID.randomUUID();
        GameEntity game = gameEntity(gameId, "ABCDEFGH", playerXId, playerOId, GameStatus.IN_PROGRESS, Symbol.X);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        MakeMoveRequest request = new MakeMoveRequest(playerOId, 0, 0);

        // Act and Assert
        assertThrows(IllegalGameStateException.class, () -> gameService.makeMove(gameId, request));
        verify(gameRepository, never()).save(any(GameEntity.class));
    }

    @Test
    void makeMoveThrowsIllegalGameStateExceptionWhenGameIsNotInProgress() {
        // Arrange
        UUID gameId = UUID.randomUUID();
        UUID playerXId = UUID.randomUUID();
        UUID playerOId = UUID.randomUUID();
        GameEntity game = gameEntity(gameId, "ABCDEFGH", playerXId, playerOId, GameStatus.FINISHED, null);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        MakeMoveRequest request = new MakeMoveRequest(playerXId, 0, 0);

        // Act and Assert
        assertThrows(IllegalGameStateException.class, () -> gameService.makeMove(gameId, request));
        verify(gameRepository, never()).save(any(GameEntity.class));
    }

    @Test
    void makeMoveThrowsIllegalGameStateExceptionWhenRequestingPlayerIsNotPartOfGame() {
        // Arrange
        UUID gameId = UUID.randomUUID();
        UUID playerXId = UUID.randomUUID();
        UUID playerOId = UUID.randomUUID();
        UUID requestingPlayerId = UUID.randomUUID();
        GameEntity game = gameEntity(gameId, "ABCDEFGH", playerXId, playerOId, GameStatus.IN_PROGRESS, Symbol.X);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        MakeMoveRequest request = new MakeMoveRequest(requestingPlayerId, 0, 0);

        // Act and Assert
        assertThrows(IllegalGameStateException.class, () -> gameService.makeMove(gameId, request));
        verify(gameRepository, never()).save(any(GameEntity.class));
    }

    @Test
    void makeMoveFinishesGameAndUpdatesStatisticsRoomAndEventsOnWinningMove() {
        // Arrange
        UUID gameId = UUID.randomUUID();
        UUID playerXId = UUID.randomUUID();
        UUID playerOId = UUID.randomUUID();
        String roomCode = "ABCDEFGH";
        GameEntity game = gameEntity(gameId, roomCode, playerXId, playerOId, GameStatus.IN_PROGRESS, Symbol.X);
        game.setMoveCount(4);
        RoomEntity room = roomEntity(roomCode, playerXId, playerOId, gameId, RoomStatus.IN_GAME);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(roomRepository.findById(roomCode)).thenReturn(Optional.of(room));
        when(gameEngine.hasWinner(any(CellValue[][].class), eq(Symbol.X))).thenReturn(true);
        when(gameRepository.save(any(GameEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roomRepository.save(any(RoomEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        MakeMoveRequest request = new MakeMoveRequest(playerXId, 2, 2);

        // Act
        gameService.makeMove(gameId, request);

        // Assert
        ArgumentCaptor<GameEntity> savedGame = ArgumentCaptor.forClass(GameEntity.class);
        verify(gameRepository).save(savedGame.capture());
        ArgumentCaptor<RoomEntity> savedRoom = ArgumentCaptor.forClass(RoomEntity.class);
        verify(roomRepository).save(savedRoom.capture());
        verify(gameEngine).placeSymbol(any(CellValue[][].class), eq(2), eq(2), eq(Symbol.X));
        verify(playerStatisticsService).recordWinLoss(playerXId, playerOId);

        ArgumentCaptor<Object> publishedEvents = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, times(2)).publishEvent(publishedEvents.capture());
        List<Object> events = publishedEvents.getAllValues();

        assertAll(
                () -> assertEquals(GameStatus.FINISHED.name(), savedGame.getValue().getStatus()),
                () -> assertEquals(GameResult.WIN.name(), savedGame.getValue().getResult()),
                () -> assertEquals(playerXId, savedGame.getValue().getWinnerId()),
                () -> assertNull(savedGame.getValue().getNextTurn()),
                () -> assertEquals(5, savedGame.getValue().getMoveCount()),
                () -> assertEquals(RoomStatus.GAME_FINISHED.name(), savedRoom.getValue().getStatus()),
                () -> assertTrue(events.stream().anyMatch(GameChangedEvent.class::isInstance)),
                () -> assertTrue(events.stream().anyMatch(RoomChangedEvent.class::isInstance))
        );
    }

    @Test
    void makeMoveFinishesGameAndUpdatesStatisticsAndRoomOnDrawingMove() {
        // Arrange
        UUID gameId = UUID.randomUUID();
        UUID playerXId = UUID.randomUUID();
        UUID playerOId = UUID.randomUUID();
        String roomCode = "ABCDEFGH";
        GameEntity game = gameEntity(gameId, roomCode, playerXId, playerOId, GameStatus.IN_PROGRESS, Symbol.O);
        game.setMoveCount(8);
        RoomEntity room = roomEntity(roomCode, playerXId, playerOId, gameId, RoomStatus.IN_GAME);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(roomRepository.findById(roomCode)).thenReturn(Optional.of(room));
        when(gameEngine.hasWinner(any(CellValue[][].class), eq(Symbol.O))).thenReturn(false);
        when(gameEngine.isDraw(any(CellValue[][].class))).thenReturn(true);
        when(gameRepository.save(any(GameEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roomRepository.save(any(RoomEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        MakeMoveRequest request = new MakeMoveRequest(playerOId, 2, 2);

        // Act
        gameService.makeMove(gameId, request);

        // Assert
        ArgumentCaptor<GameEntity> savedGame = ArgumentCaptor.forClass(GameEntity.class);
        verify(gameRepository).save(savedGame.capture());
        ArgumentCaptor<RoomEntity> savedRoom = ArgumentCaptor.forClass(RoomEntity.class);
        verify(roomRepository).save(savedRoom.capture());
        verify(gameEngine).placeSymbol(any(CellValue[][].class), eq(2), eq(2), eq(Symbol.O));
        verify(playerStatisticsService).recordDraw(playerXId, playerOId);

        assertAll(
                () -> assertEquals(GameStatus.FINISHED.name(), savedGame.getValue().getStatus()),
                () -> assertEquals(GameResult.DRAW.name(), savedGame.getValue().getResult()),
                () -> assertNull(savedGame.getValue().getWinnerId()),
                () -> assertNull(savedGame.getValue().getNextTurn()),
                () -> assertEquals(9, savedGame.getValue().getMoveCount()),
                () -> assertEquals(RoomStatus.GAME_FINISHED.name(), savedRoom.getValue().getStatus())
        );
    }

    @Test
    void forfeitGameFinishesGameAndRecordsOpponentWinForActiveGame() {
        // Arrange
        UUID gameId = UUID.randomUUID();
        UUID playerXId = UUID.randomUUID();
        UUID playerOId = UUID.randomUUID();
        GameEntity game = gameEntity(gameId, "ABCDEFGH", playerXId, playerOId, GameStatus.IN_PROGRESS, Symbol.X);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(gameRepository.save(any(GameEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        gameService.forfeitGame(gameId, playerXId);

        // Assert
        ArgumentCaptor<GameEntity> savedGame = ArgumentCaptor.forClass(GameEntity.class);
        verify(gameRepository).save(savedGame.capture());
        verify(playerStatisticsService).recordWinLoss(playerOId, playerXId);
        ArgumentCaptor<Object> publishedEvent = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(publishedEvent.capture());

        assertAll(
                () -> assertEquals(GameStatus.FINISHED.name(), savedGame.getValue().getStatus()),
                () -> assertEquals(GameResult.FORFEIT.name(), savedGame.getValue().getResult()),
                () -> assertEquals(playerOId, savedGame.getValue().getWinnerId()),
                () -> assertNull(savedGame.getValue().getNextTurn()),
                () -> assertTrue(publishedEvent.getValue() instanceof GameChangedEvent)
        );
    }

    @Test
    void forfeitGameDoesNothingWhenGameIsAlreadyFinished() {
        // Arrange
        UUID gameId = UUID.randomUUID();
        UUID playerXId = UUID.randomUUID();
        UUID playerOId = UUID.randomUUID();
        GameEntity game = gameEntity(gameId, "ABCDEFGH", playerXId, playerOId, GameStatus.FINISHED, null);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        // Act
        gameService.forfeitGame(gameId, playerXId);

        // Assert
        verify(gameRepository, never()).save(any(GameEntity.class));
        verifyNoInteractions(playerStatisticsService, eventPublisher);
    }

    private GameEntity gameEntity(
            UUID gameId,
            String roomCode,
            UUID playerXId,
            UUID playerOId,
            GameStatus status,
            Symbol nextTurn
    ) {
        Instant now = Instant.now();
        GameEntity game = new GameEntity();
        game.setGameId(gameId);
        game.setRoomCode(roomCode);
        game.setPlayerXId(playerXId);
        game.setPlayerOId(playerOId);
        game.setBoard(List.of(
                CellValue.EMPTY.name(),
                CellValue.EMPTY.name(),
                CellValue.EMPTY.name(),
                CellValue.EMPTY.name(),
                CellValue.EMPTY.name(),
                CellValue.EMPTY.name(),
                CellValue.EMPTY.name(),
                CellValue.EMPTY.name(),
                CellValue.EMPTY.name()
        ));
        game.setNextTurn(nextTurn == null ? null : nextTurn.name());
        game.setStatus(status.name());
        game.setCreatedAt(now);
        game.setUpdatedAt(now);
        return game;
    }

    private RoomEntity roomEntity(
            String roomCode,
            UUID ownerPlayerId,
            UUID guestPlayerId,
            UUID currentGameId,
            RoomStatus status
    ) {
        Instant now = Instant.now();
        RoomEntity room = new RoomEntity();
        room.setRoomCode(roomCode);
        room.setOwnerPlayerId(ownerPlayerId);
        room.setGuestPlayerId(guestPlayerId);
        room.setCurrentGameId(currentGameId);
        room.setStatus(status.name());
        room.setCreatedAt(now);
        room.setUpdatedAt(now);
        return room;
    }
}
