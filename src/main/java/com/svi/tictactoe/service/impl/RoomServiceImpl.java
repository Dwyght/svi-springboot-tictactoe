package com.svi.tictactoe.service.impl;

import com.svi.tictactoe.constant.GameConstants;
import com.svi.tictactoe.domain.Game;
import com.svi.tictactoe.domain.Room;
import com.svi.tictactoe.dto.request.room.CreateRoomRequest;
import com.svi.tictactoe.dto.request.room.JoinRoomRequest;
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
import com.svi.tictactoe.service.RoomService;
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

    private final SecureRandom secureRandom = new SecureRandom();

    public RoomServiceImpl(RoomRepository roomRepository, PlayerRepository playerRepository, GameRepository gameRepository, ActiveRoomRepository activeRoomRepository, RoomMapper roomMapper, RoomPersistenceMapper roomPersistenceMapper, GamePersistenceMapper gamePersistenceMapper) {
        this.roomRepository = roomRepository;
        this.playerRepository = playerRepository;
        this.gameRepository = gameRepository;
        this.activeRoomRepository = activeRoomRepository;
        this.roomMapper = roomMapper;
        this.roomPersistenceMapper = roomPersistenceMapper;
        this.gamePersistenceMapper = gamePersistenceMapper;
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
        Room savedRoom = roomPersistenceMapper.toDomain(savedEntity);
        return roomMapper.toResponse(savedRoom);
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

        Game game = new Game(UUID.randomUUID(), room.getRoomCode(), room.getOwnerPlayerId(), guestPlayerId);
        GameEntity gameEntity = gamePersistenceMapper.toEntity(game);
        gameRepository.save(gameEntity);

        room.setGuestPlayerId(guestPlayerId);
        room.setCurrentGameId(game.getGameId());
        room.setStatus(RoomStatus.IN_GAME);
        room.setUpdatedAt(Instant.now());
        RoomEntity savedRoomEntity = roomRepository.save(roomPersistenceMapper.toEntity(room));

        ActiveRoomEntity activeRoom = new ActiveRoomEntity();
        activeRoom.setPlayerId(guestPlayerId);
        activeRoom.setRoomCode(roomCode);
        activeRoomRepository.save(activeRoom);
        Room savedRoom = roomPersistenceMapper.toDomain(savedRoomEntity);
        return roomMapper.toResponse(savedRoom);
    }

    private void validatePlayerExists(UUID playerId) {
        if (!playerRepository.existsById(playerId)) {
            throw new ResourceNotFoundException(ErrorMessage.PLAYER_NOT_FOUND.format(playerId)
            );
        }
    }

    private void validatePlayerHasNoActiveRoom(UUID playerId) {
        if (activeRoomRepository.existsById(playerId)) {
            throw new IllegalGameStateException(ErrorMessage.PLAYER_ALREADY_IN_ACTIVE_ROOM.format(playerId)
            );
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
}