package com.svi.tictactoe.repository;

import com.svi.tictactoe.entity.RoomByStatusEntity;
import com.svi.tictactoe.entity.RoomStatusKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomStatusRepository extends CassandraRepository<RoomByStatusEntity, RoomStatusKey> {

    /**
     * Retrieves rooms in a single status partition in clustering-key order.
     *
     * @param status the room status to query
     * @return rooms ordered by creation time descending and room code ascending
     */
    List<RoomByStatusEntity> findByKeyStatus(String status);
}
