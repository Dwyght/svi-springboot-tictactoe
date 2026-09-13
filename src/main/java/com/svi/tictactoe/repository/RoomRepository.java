package com.svi.tictactoe.repository;

import com.svi.tictactoe.entity.RoomEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends CassandraRepository<RoomEntity, String> {
}