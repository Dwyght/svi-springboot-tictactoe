package com.svi.tictactoe.initializer;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApplicationStartupInitializer implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationStartupInitializer.class);

    private static final List<String> REQUIRED_TABLES = List.of(
            "players_by_id",
            "rooms_by_code",
            "games_by_id",
            "active_room_by_player",
            "leaderboard"
    );

    private final CqlSession session;

    public ApplicationStartupInitializer(CqlSession session) {
        this.session = session;
    }

    @Override
    public void run(@NonNull ApplicationArguments args) {

        CqlIdentifier keyspace = session
                .getKeyspace()
                .orElseThrow(() -> new IllegalStateException("No Cassandra keyspace is configured."));

        var keyspaceMetadata = session
                .getMetadata()
                .getKeyspace(keyspace)
                .orElseThrow(() -> new IllegalStateException("Configured Cassandra keyspace does not exist: " + keyspace.asInternal()));

        for (String tableName : REQUIRED_TABLES) {
            boolean exists = keyspaceMetadata.getTable(CqlIdentifier.fromCql(tableName)).isPresent();
            if (!exists) {
                throw new IllegalStateException("Required Cassandra table does not exist: " + tableName);
            }
        }

        LOGGER.info("Application initialized successfully. Cassandra keyspace: {}", keyspace.asInternal()
        );
    }
}