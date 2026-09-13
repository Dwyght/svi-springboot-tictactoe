package com.svi.tictactoe.service.impl;

import com.svi.tictactoe.domain.Player;
import com.svi.tictactoe.dto.request.player.CreatePlayerRequest;
import com.svi.tictactoe.dto.response.player.PlayerResponse;
import com.svi.tictactoe.entity.PlayerEntity;
import com.svi.tictactoe.enums.ErrorMessage;
import com.svi.tictactoe.exception.ResourceNotFoundException;
import com.svi.tictactoe.mapper.PlayerMapper;
import com.svi.tictactoe.mapper.persistence.PlayerPersistenceMapper;
import com.svi.tictactoe.repository.PlayerRepository;
import com.svi.tictactoe.service.PlayerService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;
    private final PlayerMapper playerMapper;
    private final PlayerPersistenceMapper playerPersistenceMapper;

    public PlayerServiceImpl(PlayerRepository playerRepository, PlayerMapper playerMapper, PlayerPersistenceMapper playerPersistenceMapper) {
        this.playerRepository = playerRepository;
        this.playerMapper = playerMapper;
        this.playerPersistenceMapper = playerPersistenceMapper;
    }

    @Override
    public PlayerResponse createPlayer(CreatePlayerRequest request) {

        UUID playerId = UUID.randomUUID();
        Player player = playerMapper.toDomain(request, playerId);
        PlayerEntity entity = playerPersistenceMapper.toEntity(player);
        PlayerEntity savedEntity = playerRepository.save(entity);
        Player savedPlayer = playerPersistenceMapper.toDomain(savedEntity);

        return playerMapper.toResponse(savedPlayer);
    }

    @Override
    public PlayerResponse getPlayer(UUID playerId) {

        PlayerEntity entity = playerRepository.findById(playerId).orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.PLAYER_NOT_FOUND.format(playerId)));
        Player player = playerPersistenceMapper.toDomain(entity);
        return playerMapper.toResponse(player);
    }
}