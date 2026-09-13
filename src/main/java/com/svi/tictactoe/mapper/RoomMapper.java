package com.svi.tictactoe.mapper;

import com.svi.tictactoe.dto.response.room.RoomResponse;
import com.svi.tictactoe.entity.Room;
import org.springframework.stereotype.Component;

@Component
public class RoomMapper {

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