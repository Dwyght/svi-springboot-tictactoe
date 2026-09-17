package com.svi.tictactoe.service.impl;

import com.svi.tictactoe.domain.Player;
import com.svi.tictactoe.entity.PlayerEntity;
import com.svi.tictactoe.mapper.persistence.PlayerPersistenceMapper;
import com.svi.tictactoe.repository.PlayerRepository;
import com.svi.tictactoe.service.LeaderboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlayerStatisticsServiceImplTest {

    private PlayerRepository playerRepository;
    private LeaderboardService leaderboardService;
    private PlayerStatisticsServiceImpl playerStatisticsService;

    @BeforeEach
    void setUp() {
        playerRepository = mock(PlayerRepository.class);
        leaderboardService = mock(LeaderboardService.class);
        playerStatisticsService = new PlayerStatisticsServiceImpl(
                playerRepository,
                new PlayerPersistenceMapper(),
                leaderboardService
        );
    }

    @Test
    void recordWinLossUpdatesAndSavesBothPlayersAndLeaderboard() {
        // Arrange
        UUID winnerId = UUID.randomUUID();
        UUID loserId = UUID.randomUUID();
        PlayerEntity winner = playerEntity(winnerId, "Winner", 2, 1, 1, 4);
        PlayerEntity loser = playerEntity(loserId, "Loser", 1, 2, 0, 3);
        when(playerRepository.findById(winnerId)).thenReturn(Optional.of(winner));
        when(playerRepository.findById(loserId)).thenReturn(Optional.of(loser));

        // Act
        playerStatisticsService.recordWinLoss(winnerId, loserId);

        // Assert
        ArgumentCaptor<PlayerEntity> savedPlayers = ArgumentCaptor.forClass(PlayerEntity.class);
        verify(playerRepository, times(2)).save(savedPlayers.capture());
        List<PlayerEntity> saved = savedPlayers.getAllValues();
        PlayerEntity savedWinner = saved.get(0);
        PlayerEntity savedLoser = saved.get(1);

        assertAll(
                () -> assertEquals(winnerId, savedWinner.getPlayerId()),
                () -> assertEquals(3, savedWinner.getWins()),
                () -> assertEquals(1, savedWinner.getLosses()),
                () -> assertEquals(1, savedWinner.getDraws()),
                () -> assertEquals(5, savedWinner.getGamesPlayed()),
                () -> assertEquals(loserId, savedLoser.getPlayerId()),
                () -> assertEquals(1, savedLoser.getWins()),
                () -> assertEquals(3, savedLoser.getLosses()),
                () -> assertEquals(0, savedLoser.getDraws()),
                () -> assertEquals(4, savedLoser.getGamesPlayed())
        );

        ArgumentCaptor<Player[]> leaderboardPlayers = ArgumentCaptor.forClass(Player[].class);
        verify(leaderboardService).updatePlayers(leaderboardPlayers.capture());
        Player[] updatedPlayers = leaderboardPlayers.getValue();
        assertAll(
                () -> assertEquals(winnerId, updatedPlayers[0].getPlayerId()),
                () -> assertEquals(3, updatedPlayers[0].getWins()),
                () -> assertEquals(5, updatedPlayers[0].getGamesPlayed()),
                () -> assertEquals(loserId, updatedPlayers[1].getPlayerId()),
                () -> assertEquals(3, updatedPlayers[1].getLosses()),
                () -> assertEquals(4, updatedPlayers[1].getGamesPlayed())
        );
    }

    @Test
    void recordDrawUpdatesAndSavesBothPlayersAndLeaderboard() {
        // Arrange
        UUID playerXId = UUID.randomUUID();
        UUID playerOId = UUID.randomUUID();
        PlayerEntity playerX = playerEntity(playerXId, "Player X", 2, 1, 1, 4);
        PlayerEntity playerO = playerEntity(playerOId, "Player O", 1, 2, 2, 5);
        when(playerRepository.findById(playerXId)).thenReturn(Optional.of(playerX));
        when(playerRepository.findById(playerOId)).thenReturn(Optional.of(playerO));

        // Act
        playerStatisticsService.recordDraw(playerXId, playerOId);

        // Assert
        ArgumentCaptor<PlayerEntity> savedPlayers = ArgumentCaptor.forClass(PlayerEntity.class);
        verify(playerRepository, times(2)).save(savedPlayers.capture());
        List<PlayerEntity> saved = savedPlayers.getAllValues();
        PlayerEntity savedPlayerX = saved.get(0);
        PlayerEntity savedPlayerO = saved.get(1);

        assertAll(
                () -> assertEquals(playerXId, savedPlayerX.getPlayerId()),
                () -> assertEquals(2, savedPlayerX.getWins()),
                () -> assertEquals(1, savedPlayerX.getLosses()),
                () -> assertEquals(2, savedPlayerX.getDraws()),
                () -> assertEquals(5, savedPlayerX.getGamesPlayed()),
                () -> assertEquals(playerOId, savedPlayerO.getPlayerId()),
                () -> assertEquals(1, savedPlayerO.getWins()),
                () -> assertEquals(2, savedPlayerO.getLosses()),
                () -> assertEquals(3, savedPlayerO.getDraws()),
                () -> assertEquals(6, savedPlayerO.getGamesPlayed())
        );

        ArgumentCaptor<Player[]> leaderboardPlayers = ArgumentCaptor.forClass(Player[].class);
        verify(leaderboardService).updatePlayers(leaderboardPlayers.capture());
        Player[] updatedPlayers = leaderboardPlayers.getValue();
        assertAll(
                () -> assertEquals(playerXId, updatedPlayers[0].getPlayerId()),
                () -> assertEquals(2, updatedPlayers[0].getDraws()),
                () -> assertEquals(5, updatedPlayers[0].getGamesPlayed()),
                () -> assertEquals(playerOId, updatedPlayers[1].getPlayerId()),
                () -> assertEquals(3, updatedPlayers[1].getDraws()),
                () -> assertEquals(6, updatedPlayers[1].getGamesPlayed())
        );
    }

    private PlayerEntity playerEntity(
            UUID playerId,
            String name,
            int wins,
            int losses,
            int draws,
            int gamesPlayed
    ) {
        PlayerEntity player = new PlayerEntity();
        player.setPlayerId(playerId);
        player.setName(name);
        player.setWins(wins);
        player.setLosses(losses);
        player.setDraws(draws);
        player.setGamesPlayed(gamesPlayed);
        return player;
    }
}
