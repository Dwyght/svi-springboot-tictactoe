package com.svi.tictactoe.service.impl;

import com.svi.tictactoe.constant.LeaderboardConstants;
import com.svi.tictactoe.domain.Player;
import com.svi.tictactoe.dto.response.leaderboard.LeaderboardEntryResponse;
import com.svi.tictactoe.dto.response.leaderboard.LeaderboardResponse;
import com.svi.tictactoe.entity.LeaderboardEntryEntity;
import com.svi.tictactoe.entity.LeaderboardKey;
import com.svi.tictactoe.mapper.LeaderboardMapper;
import com.svi.tictactoe.repository.LeaderboardRepository;
import com.svi.tictactoe.service.LeaderboardService;
import com.svi.tictactoe.event.LeaderboardChangedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LeaderboardServiceImpl implements LeaderboardService {

    private final LeaderboardRepository leaderboardRepository;
    private final LeaderboardMapper leaderboardMapper;
    private final ApplicationEventPublisher eventPublisher;

    public LeaderboardServiceImpl(
            LeaderboardRepository leaderboardRepository,
            LeaderboardMapper leaderboardMapper,
            ApplicationEventPublisher eventPublisher
    ) {
        this.leaderboardRepository = leaderboardRepository;
        this.leaderboardMapper = leaderboardMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public LeaderboardResponse getLeaderboard() {

        List<LeaderboardEntryEntity> entities = leaderboardRepository.findByKeyLeaderboardKey(LeaderboardConstants.GLOBAL_KEY);
        List<LeaderboardEntryResponse> entries = new ArrayList<>();
        for (int index = 0; index < entities.size(); index++) {
            entries.add(leaderboardMapper.toResponse(entities.get(index), index + 1));
        }

        return new LeaderboardResponse(entries);
    }

    @Override
    public void updatePlayer(Player player) {
        updatePlayers(player);
    }

    @Override
    public void updatePlayers(Player... players) {

        for (Player player : players) {
            removeExistingEntry(player);

            long scoreScaled = calculateScaledScore(player);

            LeaderboardKey key = new LeaderboardKey();
            key.setLeaderboardKey(LeaderboardConstants.GLOBAL_KEY);
            key.setScoreScaled(scoreScaled);
            key.setGamesPlayed(player.getGamesPlayed());
            key.setWins(player.getWins());
            key.setPlayerId(player.getPlayerId());

            LeaderboardEntryEntity entity = new LeaderboardEntryEntity();

            entity.setKey(key);
            entity.setPlayerName(player.getName());
            entity.setLosses(player.getLosses());
            entity.setDraws(player.getDraws());

            leaderboardRepository.save(entity);
        }

        eventPublisher.publishEvent(
                new LeaderboardChangedEvent(
                        getLeaderboard()
                )
        );
    }

    private void removeExistingEntry(Player player) {

        leaderboardRepository.findByKeyLeaderboardKey(LeaderboardConstants.GLOBAL_KEY)
                .stream()
                .filter(entity -> entity.getKey().getPlayerId().equals(player.getPlayerId()))
                .findFirst()
                .ifPresent(leaderboardRepository::delete);
    }

    private long calculateScaledScore(Player player) {

        if (player.getGamesPlayed() == 0) {
            return 0;
        }

        double performanceRate = (player.getWins() + (0.5 * player.getDraws())) / player.getGamesPlayed();
        double experienceFactor = player.getGamesPlayed() / (player.getGamesPlayed() + LeaderboardConstants.EXPERIENCE_CONSTANT);
        double score = performanceRate * experienceFactor * 100;
        return Math.round(score * LeaderboardConstants.SCORE_SCALE);
    }
}