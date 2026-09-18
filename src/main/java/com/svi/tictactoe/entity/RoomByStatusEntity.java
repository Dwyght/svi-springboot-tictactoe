package com.svi.tictactoe.entity;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("rooms_by_status")
public class RoomByStatusEntity {

    @PrimaryKey
    private RoomStatusKey key;

    @Column("owner_player_id")
    private UUID ownerPlayerId;

    @Column("guest_player_id")
    private UUID guestPlayerId;

    @Column("current_game_id")
    private UUID currentGameId;

    @Column("updated_at")
    private Instant updatedAt;

    public RoomByStatusEntity() {
    }

    public RoomStatusKey getKey() {
        return key;
    }

    public void setKey(RoomStatusKey key) {
        this.key = key;
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

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
