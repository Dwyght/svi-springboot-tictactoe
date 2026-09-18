# Tic-Tac-Toe Web Service

A Spring Boot backend for two-player Tic-Tac-Toe. Players discover joinable rooms in a live lobby, create or join rooms, submit moves through a REST API, and receive lobby, room, game, and leaderboard updates through STOMP over WebSocket. Apache Cassandra stores players, rooms, games, active-room membership, and rankings.

## Features

- Player registration and win/loss/draw statistics.
- Rooms identified by an eight-letter uppercase code.
- Live lobby listing waiting rooms, newest first.
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

The supplied [schema.cql](src/main/resources/schema.cql) now performs the complete database setup. It creates the default keyspace when necessary, selects it with `USE`, and creates all required tables. From the repository root, run:

```text
cqlsh 127.0.0.1 9042 -f src/main/resources/schema.cql
```

Do not pass `-k batch1_2026_trainees` when initializing a new database because `cqlsh` would try to select the keyspace before the script can create it.

The script uses `NetworkTopologyStrategy` with a replication factor of `1` for `datacenter1`, which is appropriate for the default local single-node setup. For another cluster, update the keyspace name, datacenter, replication settings, and `USE` statement in `schema.cql` before running it. Configure `CASSANDRA_KEYSPACE` and `CASSANDRA_LOCAL_DATACENTER` to match. If the keyspace already exists, `CREATE KEYSPACE IF NOT EXISTS` does not modify its existing replication settings.

If `cqlsh` runs in another environment, such as a container, make [schema.cql](src/main/resources/schema.cql) available there and adjust the file path.

The Cassandra schema supports the application's query patterns through these tables:

| Table | Query pattern and purpose |
| --- | --- |
| `players_by_id` | Retrieve a player's details and statistics by `player_id`. |
| `rooms_by_code` | Retrieve membership, status, and current game by `room_code`. |
| `rooms_by_status` | Read the `WAITING` partition for the lobby, ordered by `created_at DESC`, then `room_code ASC`; avoids scanning all rooms. |
| `games_by_id` | Retrieve board state, turns, results, and timestamps by `game_id`. |
| `active_room_by_player` | Check membership by `player_id` to enforce one active room per player without searching room records. |
| `leaderboard` | Read the `GLOBAL` partition in ranking order; stores statistics and score-based clustering keys for ordered retrieval. |

`RoomServiceImpl` writes the main room row and applicable lobby insert/delete in one logged batch. Only `WAITING` rooms have lobby rows; joining or closing a waiting room removes its lobby entry. Existing rooms are not automatically backfilled into this table when upgrading.

The application does not execute `schema.cql` or create database objects automatically, so run the script before starting the service. `ApplicationStartupInitializer` checks the configured keyspace and the five original tables; it currently does not check `rooms_by_status`, which must also exist for lobby reads and room mutations.

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

## API conventions

| Item | Convention |
| --- | --- |
| Player and game IDs | UUIDs represented as JSON strings; generated by the server. |
| Room code | Generated as eight uppercase letters (`A`–`Z`); use the returned code as-is. |
| Board | A 3 x 3 array; `row` and `column` are zero-based integers from `0` to `2`. |
| Cell values | `X`, `O`, or `EMPTY`. |
| Player symbols | Owner is always X, guest is always O; X starts every game, including rematches. |
| Room statuses | `WAITING`, `IN_GAME`, `GAME_FINISHED`, `CLOSED`. |
| Game statuses/results | `IN_PROGRESS` or `FINISHED`; completed results are `WIN`, `DRAW`, or `FORFEIT`. |
| Responses | Successful endpoints return their DTOs directly; errors use the centralized format below. |

Player IDs supplied by clients identify participants; the service does not implement login or authenticated player sessions.

## REST API

All paths below are relative to `/api/v1`. Send request bodies as JSON with `Content-Type: application/json`. Replace placeholder IDs with UUIDs returned by the API.

| Method | Path | Request body | Success response |
| --- | --- | --- | --- |
| POST | `/players` | `{"name":"Alice"}` | `201` with `PlayerResponse` |
| GET | `/players/{playerId}` | None | `200` with `PlayerResponse` |
| POST | `/rooms` | `{"ownerPlayerId":"<playerXId>"}` | `201` with `RoomResponse` |
| GET | `/rooms` | None | `200` with `LobbyResponse` containing joinable rooms, newest first |
| GET | `/rooms/{roomCode}` | None | `200` with `RoomResponse` |
| POST | `/rooms/{roomCode}/players` | `{"playerId":"<playerOId>"}` | `200` with `RoomResponse` |
| DELETE | `/rooms/{roomCode}/players/{playerId}` | None | `200` with the closed `RoomResponse` |
| POST | `/rooms/{roomCode}/games` | `{"playerId":"<playerXId>"}` | `200` with `RoomResponse` containing the new game ID |
| GET | `/games/{gameId}` | None | `200` with `GameResponse` |
| POST | `/games/{gameId}/moves` | `{"playerId":"<playerId>","row":0,"column":0}` | `200` with the updated `GameResponse` |
| GET | `/leaderboard` | None | `200` with `LeaderboardResponse` |

### Player endpoints

`POST /players` registers a player and returns `201 Created`. The name must be nonblank and at most 10 characters; invalid input returns `400`. Registration also creates a zero-score leaderboard entry and broadcasts the updated leaderboard.

Request:

```json
{"name":"Alice"}
```

Example response:

```json
{
  "playerId": "11111111-1111-4111-8111-111111111111",
  "name": "Alice",
  "wins": 0,
  "losses": 0,
  "draws": 0,
  "gamesPlayed": 0
}
```

`GET /players/{playerId}` returns the player and current statistics with `200 OK`, or `404` if the player does not exist.

### Room endpoints

`POST /rooms` creates a `WAITING` room and returns `201 Created` with its code. The owner must exist (`404` otherwise) and must not already have an active room (`409`). Creation records active-room membership and makes the room visible in the lobby; no game exists yet.

```json
{"ownerPlayerId":"11111111-1111-4111-8111-111111111111"}
```

Example response:

```json
{
  "roomCode": "ABCDEFGH",
  "ownerPlayerId": "11111111-1111-4111-8111-111111111111",
  "guestPlayerId": null,
  "currentGameId": null,
  "status": "WAITING",
  "createdAt": "2026-09-18T08:00:00Z",
  "updatedAt": "2026-09-18T08:00:00Z"
}
```

`GET /rooms` returns `200 OK` with `{"rooms":[...]}`, containing `RoomResponse` objects for waiting rooms, newest first. An empty lobby is `{"rooms":[]}`. `GET /rooms/{roomCode}` returns one room with `200 OK`, or `404` for an unknown code.

`POST /rooms/{roomCode}/players` joins as the guest and returns `200 OK` with the updated room. It automatically creates the first game, fills `currentGameId`, changes the room to `IN_GAME`, and removes it from the lobby.

```json
{"playerId":"22222222-2222-4222-8222-222222222222"}
```

The guest must exist and have no active room. Only a `WAITING` room without a guest can be joined, and the owner cannot join as the guest. Missing player/room returns `404`; membership or room-state conflicts return `409`.

`POST /rooms/{roomCode}/games` creates another game and returns `200 OK` with the room's new `currentGameId`.

```json
{"playerId":"11111111-1111-4111-8111-111111111111"}
```

Only the owner may request it. The room must be `GAME_FINISHED` with a guest and current game ID present; these checks return `409` on failure. An unknown room returns `404`. The new game starts with an empty board; previous game records and cumulative player statistics are retained.

`DELETE /rooms/{roomCode}/players/{playerId}` returns `200 OK` with the closed room. Either participant may leave; this closes the room for both and clears their active-room membership. Leaving during an active game records a forfeit: the opponent wins and the leaving player loses. Leaving a waiting or finished room does not add a game result. Missing room returns `404`; a nonparticipant or a repeated leave from a closed room returns `409`.

Where a request body includes a player ID field, it is a required UUID. Malformed UUIDs or missing required fields return `400`.

### Game endpoints

`GET /games/{gameId}` returns `200 OK` with the board, player IDs, `nextTurn`, status, result, winner, move count, and timestamps; an unknown game returns `404`.

`POST /games/{gameId}/moves` submits a move and returns `200 OK` with the updated `GameResponse`.

```json
{"playerId":"11111111-1111-4111-8111-111111111111","row":0,"column":0}
```

The backend derives X or O by matching `playerId` against the game's `playerXId` and `playerOId`; clients do not select a symbol. The game must be in progress, the requester must participate, and their symbol must match `nextTurn`. The target cell must be empty.

| Rejection | HTTP status |
| --- | --- |
| Missing game | `404` |
| Missing/invalid coordinates or occupied cell | `400` |
| Finished game, nonparticipant, or wrong turn | `409` |

After placing the symbol, the service checks for a winning row, column, or diagonal before checking for a full-board draw. Otherwise it switches turns. Wins/draws set the game to `FINISHED` and the room to `GAME_FINISHED`; `PlayerStatisticsService` updates both players' counters and games played, saves them, and refreshes their leaderboard entries. Ordinary moves do not change statistics.

### Leaderboard

`GET /leaderboard` returns `200 OK` with `{"entries":[...]}` in ranking order. Each entry contains `rank`, `playerId`, `playerName`, `wins`, `losses`, `draws`, `gamesPlayed`, and `score`. Ranks start at 1; an empty leaderboard returns `{"entries":[]}`. See the scoring formula below.

## Game and room lifecycle

```text
Room:
create -> WAITING --guest joins--> IN_GAME --win/draw--> GAME_FINISHED
                                    ^                       |
                                    +--- owner rematch -----+

WAITING / IN_GAME / GAME_FINISHED --participant leaves--> CLOSED

Game:
join/rematch -> IN_PROGRESS --win/draw/forfeit--> FINISHED
```

A finished room remains active for both players until someone leaves; neither can create or join another room before then. Closed rooms cannot be joined or rematched. Finished games have `nextTurn: null` and an `endedAt` timestamp; draws have `winnerId: null`. A rematch creates a new game ID rather than reopening the old game.

## Gameplay / Postman demo

1. Call `POST /players` twice. Save the returned IDs as `playerXId` and `playerOId`.
2. Call `POST /rooms` with X's ID as `ownerPlayerId`; save `roomCode`.
3. Call `GET /rooms` to see the waiting room. Subscribe to `/topic/lobby` and `/topic/rooms/{roomCode}` for live updates.
4. Call `POST /rooms/{roomCode}/players` with O's ID; save `currentGameId` as `gameId`. The room leaves the lobby.
5. Call `GET /games/{gameId}` and subscribe to `/topic/games/{gameId}`.
6. Submit the five moves below to `POST /games/{gameId}/moves` to finish with an X win.
7. Read both player records and `GET /leaderboard`: X gains a win, O gains a loss, and each gains one game played.
8. Call `POST /rooms/{roomCode}/games` using X's ID. Save the new `currentGameId` and subscribe to its game topic.
9. Call the DELETE leave endpoint as either player. The new active game ends by forfeit and the room becomes `CLOSED`.

The supplied Postman collection automatically stores `playerXId`, `playerOId`, `roomCode`, and `gameId` from creation/join/rematch responses. Run its requests in this order rather than running state-dependent checks indiscriminately.

Sample X win:

| Turn | Player | Row | Column |
| --- | --- | --- | --- |
| 1 | X | 0 | 0 |
| 2 | O | 1 | 0 |
| 3 | X | 0 | 1 |
| 4 | O | 1 | 1 |
| 5 | X | 0 | 2 |

## Error responses

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

| Destination | JSON payload | Published when |
| --- | --- | --- |
| `/topic/rooms/{roomCode}` | Updated `RoomResponse` | Room creation, joining, leaving, rematch, or a game's win/draw changes room state (`RoomChangedEvent`). |
| `/topic/games/{gameId}` | Updated `GameResponse` | An accepted move is saved, including the finishing move, or an active game is forfeited (`GameChangedEvent`). |
| `/topic/leaderboard` | Updated `LeaderboardResponse` | Player registration or win/loss/draw statistics refresh leaderboard entries (`LeaderboardChangedEvent`). |
| `/topic/lobby` | Updated `LobbyResponse` | Room creation, joining, leaving, or rematch triggers a fresh lobby query, even if the list is unchanged (`LobbyChangedEvent`). |

Services publish application events, and `RealtimeEventListener` forwards their response payloads through `SimpMessagingTemplate`. Clients receive the response object directly, without the Java event wrapper.

Game actions are submitted through REST. Although `/app` is configured as the application destination prefix, there are no client-command STOMP handlers in this project. The endpoint uses native WebSocket, without SockJS.

Subscribe to the room topic to learn its `currentGameId`, and subscribe to the new game topic after joining or starting a rematch. Game creation itself does not publish `GameChangedEvent`; use `GET /games/{gameId}` to read the initial board. Use the REST GET endpoints to obtain initial state or refresh state after reconnecting; subscriptions do not replay previous events.

## Leaderboard scoring

For players who have completed games:

```text
performanceRate  = (wins + 0.5 * draws) / gamesPlayed
experienceFactor = gamesPlayed / (gamesPlayed + 10)
score            = performanceRate * experienceFactor * 100
```

Players with no games have a score of zero. Scores are stored as `round(score * 10,000)` and converted back for responses. Rankings sort by score descending, games played descending, wins descending, and player UUID ascending. A forfeit counts as a win for the opponent and a loss for the leaving player.

## Architecture and project structure

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

`GameServiceImpl` manages game state transitions and delegates completed-game statistics to `PlayerStatisticsService`. That service increments wins, losses, draws, and games played, persists both affected players, and asks `LeaderboardService` to refresh their leaderboard entries. Leaderboard scoring remains centralized in `LeaderboardServiceImpl`.

### Architecture request flow

For `POST /api/v1/games/{gameId}/moves`, `GameController` receives a validated `MakeMoveRequest` and calls the `GameService` interface, implemented by `GameServiceImpl`:

```text
GameController -> GameService -> GameServiceImpl
  Load:  GameRepository -> GameEntity -> GamePersistenceMapper -> Game (domain)
  Apply: GameEngine validates/places the symbol; GameServiceImpl updates Game
  Save:  Game -> GamePersistenceMapper -> GameEntity -> GameRepository

Response:
saved GameEntity -> GamePersistenceMapper -> Game -> GameMapper -> GameResponse
  -> GameController -> JSON
```

The service also publishes that `GameResponse` in `GameChangedEvent` for WebSocket delivery. The persistence mapper handles the board's flat Cassandra representation; the response mapper exposes the two-dimensional board to clients.

## Testing and packaging

Import [tictactoe.postman_collection.json](src/main/resources/tictactoe.postman_collection.json) into Postman. Its `baseUrl` defaults to `http://localhost:8080`, and request scripts store `playerXId`, `playerOId`, `roomCode`, and `gameId` as collection variables. Follow the game sequence above; run state-dependent error tests when their required room/game state exists.

Run the automated test suite with JDK 25:

```powershell
.\mvnw.cmd clean test
```

The suite includes pure unit tests for `GameEngine` and `PlayerStatisticsServiceImpl`. These verify move placement, board outcomes, symbol turns, win/loss/draw statistics, player persistence, and leaderboard update delegation without starting Spring or connecting to Cassandra.

To run only the pure unit tests:

```powershell
.\mvnw.cmd '-Dtest=GameEngineTest,PlayerStatisticsServiceImplTest' test
```

The existing `TictactoeApplicationTests.contextLoads` test starts the Spring application context. A full test or package run therefore requires Cassandra to be reachable and the configured keyspace and tables to exist.

Package the application and run the resulting executable JAR:

```powershell
.\mvnw.cmd clean package
java -jar target/tictactoe-0.0.1-SNAPSHOT.jar
```

For a package without running tests, use `.\mvnw.cmd -DskipTests package`. This does not verify application startup. On Linux/macOS, replace `.\mvnw.cmd` with `./mvnw`.
