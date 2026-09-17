package com.svi.tictactoe.service.impl;

import com.svi.tictactoe.domain.Player;
import com.svi.tictactoe.entity.PlayerEntity;
import com.svi.tictactoe.enums.ErrorMessage;
import com.svi.tictactoe.exception.ResourceNotFoundException;
import com.svi.tictactoe.mapper.persistence.PlayerPersistenceMapper;
import com.svi.tictactoe.repository.PlayerRepository;
import com.svi.tictactoe.service.LeaderboardService;
import com.svi.tictactoe.service.PlayerStatisticsService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PlayerStatisticsServiceImpl implements PlayerStatisticsService {

    private final PlayerRepository playerRepository;
    private final PlayerPersistenceMapper playerPersistenceMapper;
    private final LeaderboardService leaderboardService;

    public PlayerStatisticsServiceImpl(
            PlayerRepository playerRepository,
            PlayerPersistenceMapper playerPersistenceMapper,
            LeaderboardService leaderboardService
    ) {
        this.playerRepository = playerRepository;
        this.playerPersistenceMapper = playerPersistenceMapper;
        this.leaderboardService = leaderboardService;
    }

    @Override
    public void recordWinLoss(UUID winnerId, UUID loserId) {
        Player winner = findPlayer(winnerId);
        Player loser = findPlayer(loserId);

        winner.setWins(winner.getWins() + 1);
        winner.setGamesPlayed(winner.getGamesPlayed() + 1);
        loser.setLosses(loser.getLosses() + 1);
        loser.setGamesPlayed(loser.getGamesPlayed() + 1);

        savePlayer(winner);
        savePlayer(loser);
        leaderboardService.updatePlayers(winner, loser);
    }

    @Override
    public void recordDraw(UUID playerXId, UUID playerOId) {
        Player playerX = findPlayer(playerXId);
        Player playerO = findPlayer(playerOId);

        playerX.setDraws(playerX.getDraws() + 1);
        playerX.setGamesPlayed(playerX.getGamesPlayed() + 1);
        playerO.setDraws(playerO.getDraws() + 1);
        playerO.setGamesPlayed(playerO.getGamesPlayed() + 1);

        savePlayer(playerX);
        savePlayer(playerO);
        leaderboardService.updatePlayers(playerX, playerO);
    }

    private Player findPlayer(UUID playerId) {
        PlayerEntity entity = playerRepository.findById(playerId).orElseThrow(() ->
                new ResourceNotFoundException(ErrorMessage.PLAYER_NOT_FOUND.format(playerId)));
        return playerPersistenceMapper.toDomain(entity);
    }

    private void savePlayer(Player player) {
        playerRepository.save(playerPersistenceMapper.toEntity(player));
    }
}
