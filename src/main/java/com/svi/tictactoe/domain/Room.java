package com.svi.tictactoe.domain;

import com.svi.tictactoe.enums.RoomStatus;

import java.time.Instant;
import java.util.UUID;

public class Room {

    private String roomCode;
    private UUID ownerPlayerId;
    private UUID guestPlayerId;
    private UUID currentGameId;
    private RoomStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public Room() {
    }

    public Room(String roomCode, UUID ownerPlayerId) {
        this.roomCode = roomCode;
        this.ownerPlayerId = ownerPlayerId;
        this.status = RoomStatus.WAITING;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public UUID getOwnerPlayerId() {
        return ownerPlayerId;
    }

    public void setOwnerPlayerId(UUID ownerPlayerId) {
        this.ownerPlayerId = ownerPlayerId;
    }

    public UUID getGuestPlayerId() {
        return guestPlayerId;
    }

    public void setGuestPlayerId(UUID guestPlayerId) {
        this.guestPlayerId = guestPlayerId;
    }

    public UUID getCurrentGameId() {
        return currentGameId;
    }

    public void setCurrentGameId(UUID currentGameId) {
        this.currentGameId = currentGameId;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
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
}
