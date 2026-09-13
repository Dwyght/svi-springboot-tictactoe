package com.svi.tictactoe.repository;

import com.svi.tictactoe.entity.LeaderboardEntryEntity;
import com.svi.tictactoe.entity.LeaderboardKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaderboardRepository extends CassandraRepository<LeaderboardEntryEntity, LeaderboardKey> {

    List<LeaderboardEntryEntity> findByKeyLeaderboardKey(String leaderboardKey);
}