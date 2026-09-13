package com.svi.tictactoe.repository;

import com.svi.tictactoe.entity.PlayerEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PlayerRepository extends CassandraRepository<PlayerEntity, UUID> {
}