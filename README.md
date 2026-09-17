# Tic-Tac-Toe Web Service

A Spring Boot backend for two-player Tic-Tac-Toe. Players create or join rooms, submit moves through a REST API, and receive room, game, and leaderboard updates through STOMP over WebSocket. Apache Cassandra stores players, rooms, games, active-room membership, and rankings.

## Features

- Player registration and win/loss/draw statistics.
- Rooms identified by an eight-letter uppercase code.
- Automatic first-game creation when a guest joins.
- Turn validation, occupied-cell checks, win detection, and draws on a 3 x 3 board.
- Owner-requested rematches and forfeits when a player leaves an active game.
- Global leaderboard with performance and experience-based scoring.
- Real-time broadcasts, centralized HTTP error handling, and execution logging.

## Technology

| Component | Technology |
| --- | --- |
| Runtime | Java 25 |
| Framework | Spring Boot 4.1.1 |
| HTTP API | Spring Web MVC and Jakarta Bean Validation |
| Persistence | Spring Data Cassandra |
| Messaging | Spring WebSocket, STOMP, and an in-memory simple broker |
| Logging | SLF4J and Spring AOP |
| Monitoring | Spring Boot Actuator |
| Build | Maven Wrapper (Maven 3.9.16) |

## Setup

### 1. Prerequisites

- JDK 25.
- A running Cassandra instance and access to `cqlsh`.
- Network access for the Maven Wrapper's first dependency download.
- Optional: IntelliJ IDEA and Postman.

Run commands from the repository root. On Windows PowerShell, check the Java runtime used by both the shell and Maven:

```powershell
java -version
.\mvnw.cmd -version
```

Both should report Java 25. The `java.version` property in `pom.xml` sets the compilation target; it does not select the JDK that launches Maven. Set `JAVA_HOME` to your JDK 25 installation if needed.

In IntelliJ, import `pom.xml` as a Maven project and select JDK 25 for the project/module SDK, Maven runner, Maven importer, and application run configuration.

### 2. Prepare Cassandra

The default connection uses `127.0.0.1:9042`, local datacenter `datacenter1`, and keyspace `batch1_2026_trainees`.

If the keyspace does not already exist, this example creates it for a local, single-node Cassandra instance whose datacenter is named `datacenter1`. Run it in `cqlsh`:

```cql
CREATE KEYSPACE IF NOT EXISTS batch1_2026_trainees
WITH replication = {
    'class': 'NetworkTopologyStrategy',
    'datacenter1': 1
};
```

Use your cluster's actual datacenter name and appropriate replication settings when using another environment. From the repository root, apply the supplied schema to the selected keyspace:

```text
cqlsh 127.0.0.1 9042 -k batch1_2026_trainees -f src/main/resources/schema.cql
```

If `cqlsh` runs in another environment, such as a container, make [schema.cql](src/main/resources/schema.cql) available there and adjust the file path.

The application checks for these tables at startup:

| Table | Purpose |
| --- | --- |
| `players_by_id` | Player details and statistics |
| `rooms_by_code` | Room membership, status, and current game |
| `games_by_id` | Board state, turns, results, and timestamps |
| `active_room_by_player` | Lookup used to enforce one active room per player |
| `leaderboard` | Entries ordered by ranking within the `GLOBAL` partition |

Startup validation checks that the keyspace and tables exist; it does not create them or apply the schema automatically.

### 3. Configuration

Defaults are defined in [application.properties](src/main/resources/application.properties).

| Environment variable | Default | Purpose |
| --- | --- | --- |
| `CASSANDRA_KEYSPACE` | `batch1_2026_trainees` | Cassandra keyspace |
| `CASSANDRA_CONTACT_POINTS` | `127.0.0.1:9042` | Cassandra contact points |
| `CASSANDRA_LOCAL_DATACENTER` | `datacenter1` | Driver's local datacenter |

For example, in PowerShell:

```powershell
$env:CASSANDRA_KEYSPACE = 'batch1_2026_trainees'
$env:CASSANDRA_CONTACT_POINTS = '127.0.0.1:9042'
$env:CASSANDRA_LOCAL_DATACENTER = 'datacenter1'
```

The `app.allowed-origin-patterns` property currently allows `http://localhost:*` and `http://127.0.0.1:*` for the WebSocket handshake. This setting does not configure REST CORS.

### 4. Run the application

Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
./mvnw spring-boot:run
```

Alternatively, run `com.svi.tictactoe.TictactoeApplication` in IntelliJ using JDK 25.

Default addresses:

- REST API: `http://localhost:8080/api/v1`
- STOMP handshake: `ws://localhost:8080/ws`
- Health endpoint: `http://localhost:8080/actuator/health`

## REST API

All paths below are relative to `/api/v1`. Send request bodies as JSON with `Content-Type: application/json`. Replace placeholder IDs with UUIDs returned by the API.

| Method | Path | Request body | Success response |
| --- | --- | --- | --- |
| POST | `/players` | `{"name":"Alice"}` | `201` with `PlayerResponse` |
| GET | `/players/{playerId}` | None | `200` with `PlayerResponse` |
| POST | `/rooms` | `{"ownerPlayerId":"<playerXId>"}` | `201` with `RoomResponse` |
| GET | `/rooms/{roomCode}` | None | `200` with `RoomResponse` |
| POST | `/rooms/{roomCode}/players` | `{"playerId":"<playerOId>"}` | `200` with `RoomResponse` |
| DELETE | `/rooms/{roomCode}/players/{playerId}` | None | `200` with the closed `RoomResponse` |
| POST | `/rooms/{roomCode}/games` | `{"playerId":"<playerXId>"}` | `200` with `RoomResponse` containing the new game ID |
| GET | `/games/{gameId}` | None | `200` with `GameResponse` |
| POST | `/games/{gameId}/moves` | `{"playerId":"<playerId>","row":0,"column":0}` | `200` with the updated `GameResponse` |
| GET | `/leaderboard` | None | `200` with `LeaderboardResponse` |

Request validation:

- Player names must be nonblank and at most 10 characters long.
- Player IDs in request bodies are required UUIDs.
- Move rows and columns are required integers from `0` to `2`.
- Only a participating player may move, and only on their turn.

The service currently accepts player IDs supplied by the client; it does not implement login or authenticated player sessions.

### Play a game

1. Call `POST /players` twice to create X and O. Save each response's `playerId`.
2. Call `POST /rooms` with X's ID as `ownerPlayerId`. Save the returned `roomCode`.
3. Call `POST /rooms/{roomCode}/players` with O's ID. Joining creates the first game automatically. Save `currentGameId` as `gameId`.
4. Read `/games/{gameId}` and submit moves to `/games/{gameId}/moves`.
5. After a win or draw, inspect `/leaderboard` and the player records for updated statistics.
6. The owner may call `POST /rooms/{roomCode}/games` to start another game. Save the new `currentGameId`.
7. Either participant may leave using the DELETE endpoint. This closes the room for both players and clears both active-room entries. Leaving during play forfeits the game to the opponent.

The owner is always X, the guest is always O, and X starts each game, including rematches. A finished room remains active for both players until one of them explicitly leaves.

For a sample X win, submit these moves in order:

| Turn | Player | Row | Column |
| --- | --- | --- | --- |
| 1 | X | 0 | 0 |
| 2 | O | 1 | 0 |
| 3 | X | 0 | 1 |
| 4 | O | 1 | 1 |
| 5 | X | 0 | 2 |

Room states are `WAITING`, `IN_GAME`, `GAME_FINISHED`, and `CLOSED`. A rematch requires the owner to request it while the room is `GAME_FINISHED`, with a guest and current game ID still present.

Game states are `IN_PROGRESS` and `FINISHED`; completed results are `WIN`, `DRAW`, or `FORFEIT`. The board is returned as a two-dimensional array of `X`, `O`, and `EMPTY` values. Finished games have no next turn; draws have no winner.

### Error responses

Errors handled by `GlobalExceptionHandler` use this structure:

```json
{
  "timestamp": "2026-09-17T08:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Room with code ABCDEFGH was not found."
}
```

| Status | Examples |
| --- | --- |
| `400 Bad Request` | Invalid request body, malformed UUID, validation failure, out-of-range move, occupied cell |
| `404 Not Found` | Missing player, room, or game |
| `409 Conflict` | Player already in an active room, wrong turn, nonparticipant action, unavailable room/rematch, already closed room |
| `500 Internal Server Error` | Unexpected exception; response contains a generic message |

The response contains a human-readable message, not an `ErrorMessage` enum code. Validation order affects which message is returned: for example, an owner trying to join their own room normally hits the active-room check before the own-room check.

## Real-time updates

Connect a STOMP-capable client to `ws://localhost:8080/ws`, then subscribe to the relevant destinations:

| Destination | JSON payload |
| --- | --- |
| `/topic/rooms/{roomCode}` | Updated `RoomResponse` |
| `/topic/games/{gameId}` | Updated `GameResponse` |
| `/topic/leaderboard` | Updated `LeaderboardResponse` |

Services publish application events, and `RealtimeEventListener` forwards their response payloads through `SimpMessagingTemplate`. Clients receive the response object directly, without the Java event wrapper.

Game actions are submitted through REST. Although `/app` is configured as the application destination prefix, there are no client-command STOMP handlers in this project. The endpoint uses native WebSocket, without SockJS.

Subscribe to the room topic to learn its `currentGameId`, and subscribe to the new game topic after joining or starting a rematch. Use the REST GET endpoints to obtain initial state or refresh state after reconnecting; subscriptions do not replay previous events.

## Leaderboard scoring

For players who have completed games:

```text
performanceRate  = (wins + 0.5 * draws) / gamesPlayed
experienceFactor = gamesPlayed / (gamesPlayed + 10)
score            = performanceRate * experienceFactor * 100
```

Players with no games have a score of zero. Scores are stored as `round(score * 10,000)` and converted back for responses. Rankings sort by score descending, games played descending, wins descending, and player UUID ascending. A forfeit counts as a win for the opponent and a loss for the leaving player.

## Project structure

Packages under `src/main/java/com/svi/tictactoe`:

| Package | Responsibility |
| --- | --- |
| `controller` | REST endpoints and request validation |
| `service`, `service/impl` | Service contracts and workflow coordination |
| `domain` | In-memory player, room, and game models |
| `engine` | Board rules and outcome checks |
| `dto` | Request and response records |
| `mapper`, `mapper/persistence` | Conversion between API, domain, and persistence models |
| `entity`, `repository` | Cassandra mappings and data access |
| `exception` | Domain exceptions and centralized HTTP error responses |
| `event`, `realtime` | Change events and STOMP broadcasts |
| `config`, `initializer` | Configuration and startup schema checks |
| `aspect` | Controller/service execution logging |
| `constant`, `enums` | Shared values, states, and messages |

Controllers delegate to service interfaces. Service implementations coordinate domain objects, the game engine, persistence mappers, and repositories; response mappers prepare API results. Both joining and starting a subsequent game share the private `createGameForRoom` helper in `RoomServiceImpl`.

## Testing and packaging

Import [tictactoe.postman_collection.json](src/main/resources/tictactoe.postman_collection.json) into Postman. Its `baseUrl` defaults to `http://localhost:8080`, and request scripts store `playerXId`, `playerOId`, `roomCode`, and `gameId` as collection variables. Follow the game sequence above; run state-dependent error tests when their required room/game state exists.

Run the existing automated test with JDK 25:

```powershell
.\mvnw.cmd test
```

The test suite currently contains a Spring application context-loading test. It requires a reachable Cassandra instance, the configured keyspace, and all required tables. It does not provide automated gameplay unit tests.

Package the application and run the resulting executable JAR:

```powershell
.\mvnw.cmd package
java -jar target/tictactoe-0.0.1-SNAPSHOT.jar
```

For a package without running tests, use `.\mvnw.cmd -DskipTests package`. This does not verify application startup. On Linux/macOS, replace `.\mvnw.cmd` with `./mvnw`.
