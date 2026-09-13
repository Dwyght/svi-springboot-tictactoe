package com.svi.tictactoe.mapper;

import com.svi.tictactoe.constant.LeaderboardConstants;
import com.svi.tictactoe.dto.response.leaderboard.LeaderboardEntryResponse;
import com.svi.tictactoe.entity.LeaderboardEntryEntity;
import org.springframework.stereotype.Component;

@Component
public class LeaderboardMapper {

    public LeaderboardEntryResponse toResponse(LeaderboardEntryEntity entity, int rank) {
        double score = entity.getKey().getScoreScaled() / (double) LeaderboardConstants.SCORE_SCALE;

        return new LeaderboardEntryResponse(
                rank,
                entity.getKey().getPlayerId(),
                entity.getPlayerName(),
                entity.getKey().getWins(),
                entity.getLosses(),
                entity.getDraws(),
                entity.getKey().getGamesPlayed(),
                score
        );
    }
}