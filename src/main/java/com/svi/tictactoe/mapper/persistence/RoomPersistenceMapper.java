package com.svi.tictactoe.mapper.persistence;

import com.svi.tictactoe.domain.Room;
import com.svi.tictactoe.entity.RoomByStatusEntity;
import com.svi.tictactoe.entity.RoomEntity;
import com.svi.tictactoe.entity.RoomStatusKey;
import com.svi.tictactoe.enums.RoomStatus;
import org.springframework.stereotype.Component;

/**
 * Converts rooms between domain and Cassandra persistence representations.
 */
@Component
public class RoomPersistenceMapper {

    /**
     * Converts a room into its status-partitioned persistence representation.
     *
     * @param room the room domain object
     * @return the room status entity
     */
    public RoomByStatusEntity toStatusEntity(Room room) {
        RoomStatusKey key = new RoomStatusKey();
        key.setStatus(room.getStatus().name());
        key.setCreatedAt(room.getCreatedAt());
        key.setRoomCode(room.getRoomCode());

        RoomByStatusEntity entity = new RoomByStatusEntity();
        entity.setKey(key);
        entity.setOwnerPlayerId(room.getOwnerPlayerId());
        entity.setGuestPlayerId(room.getGuestPlayerId());
        entity.setCurrentGameId(room.getCurrentGameId());
        entity.setUpdatedAt(room.getUpdatedAt());
        return entity;
    }

    /**
     * Converts a status-partitioned Cassandra room entity into a domain object.
     *
     * @param entity the room status entity
     * @return the room domain object
     */
    public Room toDomain(RoomByStatusEntity entity) {
        Room room = new Room();
        room.setRoomCode(entity.getKey().getRoomCode());
        room.setStatus(RoomStatus.valueOf(entity.getKey().getStatus()));
        room.setCreatedAt(entity.getKey().getCreatedAt());
        room.setOwnerPlayerId(entity.getOwnerPlayerId());
        room.setGuestPlayerId(entity.getGuestPlayerId());
        room.setCurrentGameId(entity.getCurrentGameId());
        room.setUpdatedAt(entity.getUpdatedAt());
        return room;
    }

    /**
     * Converts a room domain object into a Cassandra entity.
     *
     * @param room the room domain object
     * @return the room entity
     */
    public RoomEntity toEntity(Room room) {
        RoomEntity entity = new RoomEntity();

        entity.setRoomCode(room.getRoomCode());
        entity.setOwnerPlayerId(room.getOwnerPlayerId());
        entity.setGuestPlayerId(room.getGuestPlayerId());
        entity.setCurrentGameId(room.getCurrentGameId());

        entity.setStatus(room.getStatus() == null ? null : room.getStatus().name());

        entity.setCreatedAt(room.getCreatedAt());
        entity.setUpdatedAt(room.getUpdatedAt());

        return entity;
    }

    /**
     * Converts a Cassandra room entity into a domain object.
     *
     * @param entity the room entity
     * @return the room domain object
     */
    public Room toDomain(RoomEntity entity) {
        Room room = new Room();

        room.setRoomCode(entity.getRoomCode());
        room.setOwnerPlayerId(entity.getOwnerPlayerId());
        room.setGuestPlayerId(entity.getGuestPlayerId());
        room.setCurrentGameId(entity.getCurrentGameId());

        room.setStatus(entity.getStatus() == null ? null : RoomStatus.valueOf(entity.getStatus()));

        room.setCreatedAt(entity.getCreatedAt());
        room.setUpdatedAt(entity.getUpdatedAt());

        return room;
    }
}
