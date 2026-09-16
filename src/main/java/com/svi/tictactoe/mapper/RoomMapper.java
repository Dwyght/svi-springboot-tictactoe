package com.svi.tictactoe.mapper;

import com.svi.tictactoe.dto.response.room.RoomResponse;
import com.svi.tictactoe.domain.Room;
import org.springframework.stereotype.Component;

/**
 * Maps room domain objects to API response models.
 */
@Component
public class RoomMapper {

    /**
     * Creates an API response from a room domain object.
     *
     * @param room the room domain object
     * @return the room response
     */
    public RoomResponse toResponse(Room room) {
        return new RoomResponse(
                room.getRoomCode(),
                room.getOwnerPlayerId(),
                room.getGuestPlayerId(),
                room.getCurrentGameId(),
                room.getStatus(),
                room.getCreatedAt(),
                room.getUpdatedAt()
        );
    }
}
