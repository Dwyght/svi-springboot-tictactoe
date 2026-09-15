package com.svi.tictactoe.service.impl;

import com.svi.tictactoe.constant.GameConstants;
import com.svi.tictactoe.domain.Game;
import com.svi.tictactoe.domain.Room;
import com.svi.tictactoe.dto.request.room.CreateRoomRequest;
import com.svi.tictactoe.dto.request.room.JoinRoomRequest;
import com.svi.tictactoe.dto.request.game.CreateGameRequest;
import com.svi.tictactoe.dto.response.room.RoomResponse;
import com.svi.tictactoe.entity.ActiveRoomEntity;
import com.svi.tictactoe.entity.GameEntity;
import com.svi.tictactoe.entity.RoomEntity;
import com.svi.tictactoe.enums.ErrorMessage;
import com.svi.tictactoe.enums.RoomStatus;
import com.svi.tictactoe.exception.IllegalGameStateException;
import com.svi.tictactoe.exception.ResourceNotFoundException;
import com.svi.tictactoe.mapper.RoomMapper;
import com.svi.tictactoe.mapper.persistence.GamePersistenceMapper;
import com.svi.tictactoe.mapper.persistence.RoomPersistenceMapper;
import com.svi.tictactoe.repository.ActiveRoomRepository;
import com.svi.tictactoe.repository.GameRepository;
import com.svi.tictactoe.repository.PlayerRepository;
import com.svi.tictactoe.repository.RoomRepository;
import com.svi.tictactoe.service.GameService;
import com.svi.tictactoe.service.RoomService;
import com.svi.tictactoe.event.RoomChangedEvent;
import org.springframework.context.ApplicationEventPublisher;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final PlayerRepository playerRepository;
    private final GameRepository gameRepository;
    private final ActiveRoomRepository activeRoomRepository;

    private final RoomMapper roomMapper;
    private final RoomPersistenceMapper roomPersistenceMapper;
    private final GamePersistenceMapper gamePersistenceMapper;
    private final GameService gameService;

    private final SecureRandom secureRandom = new SecureRandom();
    private final ApplicationEventPublisher eventPublisher;

    public RoomServiceImpl(
            RoomRepository roomRepository,
            PlayerRepository playerRepository,
            GameRepository gameRepository,
            ActiveRoomRepository activeRoomRepository,
            RoomMapper roomMapper,
            RoomPersistenceMapper roomPersistenceMapper,
            GamePersistenceMapper gamePersistenceMapper,
            GameService gameService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.roomRepository = roomRepository;
        this.playerRepository = playerRepository;
        this.gameRepository = gameRepository;
        this.activeRoomRepository = activeRoomRepository;
        this.roomMapper = roomMapper;
        this.roomPersistenceMapper = roomPersistenceMapper;
        this.gamePersistenceMapper = gamePersistenceMapper;
        this.gameService = gameService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public RoomResponse createRoom(CreateRoomRequest request) {

        UUID ownerPlayerId = request.ownerPlayerId();

        validatePlayerExists(ownerPlayerId);
        validatePlayerHasNoActiveRoom(ownerPlayerId);

        String roomCode = generateUniqueRoomCode();
        Room room = new Room(roomCode, ownerPlayerId);
        RoomEntity savedEntity = roomRepository.save(roomPersistenceMapper.toEntity(room));

        ActiveRoomEntity activeRoom = new ActiveRoomEntity();
        activeRoom.setPlayerId(ownerPlayerId);
        activeRoom.setRoomCode(roomCode);
        activeRoomRepository.save(activeRoom);
        return publishRoomChanged(savedEntity);
    }

    @Override
    public RoomResponse getRoom(String roomCode) {
        RoomEntity entity = findRoomEntity(roomCode);
        Room room = roomPersistenceMapper.toDomain(entity);
        return roomMapper.toResponse(room);
    }

    @Override
    public RoomResponse joinRoom(String roomCode, JoinRoomRequest request) {
        UUID guestPlayerId = request.playerId();

        validatePlayerExists(guestPlayerId);
        validatePlayerHasNoActiveRoom(guestPlayerId);

        RoomEntity roomEntity = findRoomEntity(roomCode);
        Room room = roomPersistenceMapper.toDomain(roomEntity);
        validateJoin(room, guestPlayerId);

        room.setGuestPlayerId(guestPlayerId);
        Game game = createGameForRoom(room);
        room.setCurrentGameId(game.getGameId());
        room.setStatus(RoomStatus.IN_GAME);
        room.setUpdatedAt(Instant.now());
        RoomEntity savedRoomEntity = roomRepository.save(roomPersistenceMapper.toEntity(room));

        ActiveRoomEntity activeRoom = new ActiveRoomEntity();
        activeRoom.setPlayerId(guestPlayerId);
        activeRoom.setRoomCode(roomCode);
        activeRoomRepository.save(activeRoom);
        return publishRoomChanged(savedRoomEntity);
    }

    @Override
    public RoomResponse leaveRoom(String roomCode, UUID playerId) {
        RoomEntity entity = findRoomEntity(roomCode);
        Room room = roomPersistenceMapper.toDomain(entity);
        validatePlayerInRoom(room, playerId);
        validateRoomNotClosed(room);

        if (room.getStatus() == RoomStatus.IN_GAME) {
            gameService.forfeitGame(room.getCurrentGameId(), playerId);
        }

        room.setStatus(RoomStatus.CLOSED);
        room.setUpdatedAt(Instant.now());
        RoomEntity savedEntity = roomRepository.save(roomPersistenceMapper.toEntity(room));
        removePlayersFromActiveRoom(room);
        return publishRoomChanged(savedEntity);
    }

    @Override
    public RoomResponse createGame(String roomCode, CreateGameRequest request) {
        UUID playerId = request.playerId();
        RoomEntity entity = findRoomEntity(roomCode);
        Room room = roomPersistenceMapper.toDomain(entity);
        validateRematch(room, playerId);

        Game game = createGameForRoom(room);
        room.setCurrentGameId(game.getGameId());
        room.setStatus(RoomStatus.IN_GAME);
        room.setUpdatedAt(Instant.now());
        RoomEntity savedEntity = roomRepository.save(roomPersistenceMapper.toEntity(room));
        return publishRoomChanged(savedEntity);
    }

    private Game createGameForRoom(Room room) {
        Game game = new Game(
                UUID.randomUUID(),
                room.getRoomCode(),
                room.getOwnerPlayerId(),
                room.getGuestPlayerId()
        );
        GameEntity savedEntity = gameRepository.save(gamePersistenceMapper.toEntity(game));
        return gamePersistenceMapper.toDomain(savedEntity);
    }

    private void validatePlayerExists(UUID playerId) {
        if (!playerRepository.existsById(playerId)) {
            throw new ResourceNotFoundException(ErrorMessage.PLAYER_NOT_FOUND.format(playerId));
        }
    }

    private void validatePlayerHasNoActiveRoom(UUID playerId) {
        if (activeRoomRepository.existsById(playerId)) {
            throw new IllegalGameStateException(ErrorMessage.PLAYER_ALREADY_IN_ACTIVE_ROOM.format(playerId));
        }
    }

    private void validateJoin(Room room, UUID guestPlayerId) {
        if (room.getOwnerPlayerId().equals(guestPlayerId)) {
            throw new IllegalGameStateException(ErrorMessage.PLAYER_CANNOT_JOIN_OWN_ROOM.getMessage());
        }
        if (room.getStatus() != RoomStatus.WAITING || room.getGuestPlayerId() != null) {
            throw new IllegalGameStateException(ErrorMessage.ROOM_NOT_AVAILABLE.format(room.getRoomCode()));
        }
    }

    private RoomEntity findRoomEntity(String roomCode) {
        return roomRepository.findById(roomCode).orElseThrow(() ->
                new ResourceNotFoundException(ErrorMessage.ROOM_NOT_FOUND.format(roomCode)));
    }

    private String generateUniqueRoomCode() {
        String roomCode;
        do {
            roomCode = generateRoomCode();
        } while (roomRepository.existsById(roomCode));
        return roomCode;
    }

    private String generateRoomCode() {
        StringBuilder roomCode = new StringBuilder(GameConstants.ROOM_CODE_LENGTH);

        for (int index = 0; index < GameConstants.ROOM_CODE_LENGTH; index++) {
            int characterIndex = secureRandom.nextInt(GameConstants.ROOM_CODE_CHARACTERS.length());
            roomCode.append(GameConstants.ROOM_CODE_CHARACTERS.charAt(characterIndex));
        }
        return roomCode.toString();
    }

    private void validatePlayerInRoom(Room room, UUID playerId) {
        boolean isOwner = room.getOwnerPlayerId().equals(playerId);
        boolean isGuest = room.getGuestPlayerId() != null && room.getGuestPlayerId().equals(playerId);
        if (!isOwner && !isGuest) {
            throw new IllegalGameStateException(ErrorMessage.PLAYER_NOT_IN_ROOM.format(playerId, room.getRoomCode()));
        }
    }

    private void validateRoomNotClosed(Room room) {
        if (room.getStatus() == RoomStatus.CLOSED) {
            throw new IllegalGameStateException(ErrorMessage.ROOM_ALREADY_CLOSED.format(room.getRoomCode()));
        }
    }
    private void removePlayersFromActiveRoom(Room room) {
        activeRoomRepository.deleteById(room.getOwnerPlayerId());
        if (room.getGuestPlayerId() != null) {
            activeRoomRepository.deleteById(room.getGuestPlayerId());
        }
    }

    private void validateRematch(Room room, UUID playerId) {
        if (!room.getOwnerPlayerId().equals(playerId)) {
            throw new IllegalGameStateException(ErrorMessage.ONLY_ROOM_OWNER_CAN_REMATCH.getMessage());
        }
        if (room.getStatus() != RoomStatus.GAME_FINISHED || room.getGuestPlayerId() == null || room.getCurrentGameId() == null) {
            throw new IllegalGameStateException(ErrorMessage.REMATCH_NOT_AVAILABLE.format(room.getRoomCode()));
        }
    }

    private RoomResponse publishRoomChanged(RoomEntity entity) {
        Room room = roomPersistenceMapper.toDomain(entity);
        RoomResponse response = roomMapper.toResponse(room);
        eventPublisher.publishEvent(new RoomChangedEvent(response));
        return response;
    }
}
