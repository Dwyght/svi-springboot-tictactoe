package com.svi.tictactoe.entity;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("leaderboard")
public class LeaderboardEntryEntity {

    @PrimaryKey
    private LeaderboardKey key;

    @Column("player_name")
    private String playerName;

    @Column("losses")
    private int losses;

    @Column("draws")
    private int draws;

    public LeaderboardEntryEntity() {
    }

    public LeaderboardKey getKey() {
        return key;
    }

    public void setKey(LeaderboardKey key) {
        this.key = key;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public int getDraws() {
        return draws;
    }

    public void setDraws(int draws) {
        this.draws = draws;
    }
}