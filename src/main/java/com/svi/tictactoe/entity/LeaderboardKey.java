package com.svi.tictactoe.entity;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@PrimaryKeyClass
public class LeaderboardKey implements Serializable {

    @PrimaryKeyColumn(name = "leaderboard_key", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String leaderboardKey;

    @PrimaryKeyColumn(name = "score_scaled", ordinal = 1, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    private long scoreScaled;

    @PrimaryKeyColumn(name = "games_played", ordinal = 2, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    private int gamesPlayed;

    @PrimaryKeyColumn(name = "wins", ordinal = 3, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    private int wins;

    @PrimaryKeyColumn(name = "player_id", ordinal = 4, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.ASCENDING)
    private UUID playerId;

    public LeaderboardKey() {
    }

    public String getLeaderboardKey() {
        return leaderboardKey;
    }

    public void setLeaderboardKey(String leaderboardKey) {
        this.leaderboardKey = leaderboardKey;
    }

    public long getScoreScaled() {
        return scoreScaled;
    }

    public void setScoreScaled(long scoreScaled) {
        this.scoreScaled = scoreScaled;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof LeaderboardKey that)) {
            return false;
        }

        return scoreScaled == that.scoreScaled
                && gamesPlayed == that.gamesPlayed
                && wins == that.wins
                && Objects.equals(leaderboardKey, that.leaderboardKey)
                && Objects.equals(playerId, that.playerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(leaderboardKey, scoreScaled, gamesPlayed, wins, playerId);
    }
}