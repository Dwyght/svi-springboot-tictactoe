-- =========================================================
-- PLAYER
--
-- Query:
-- Find a player using player_id
--
-- Partition Key:
-- player_id
-- =========================================================

CREATE TABLE IF NOT EXISTS players_by_id (
 player_id uuid,
 name text,
 wins int,
 losses int,
 draws int,
 games_played int,
 PRIMARY KEY ((player_id))
);


-- =========================================================
-- ROOM
--
-- Query:
-- Find a room using its 8-character room code
--
-- Partition Key:
-- room_code
-- =========================================================

CREATE TABLE IF NOT EXISTS rooms_by_code (
 room_code text,
 owner_player_id uuid,
 guest_player_id uuid,
 current_game_id uuid,
 status text,
 created_at timestamp,
 updated_at timestamp,
 PRIMARY KEY ((room_code))
);


-- =========================================================
-- GAME
--
-- Query:
-- Find a game using game_id
--
-- Partition Key:
-- game_id
--
-- Board is stored as a flat list:
-- [X, O, EMPTY, EMPTY, X, EMPTY, ...]
--
-- Repository layer converts between:
-- list<text> <-> CellValue[][]
-- =========================================================

CREATE TABLE IF NOT EXISTS games_by_id (
game_id uuid,
room_code text,
player_x_id uuid,
player_o_id uuid,
board list<text>,
next_turn text,
status text,
result text,
winner_id uuid,
move_count int,
created_at timestamp,
updated_at timestamp,
ended_at timestamp,
PRIMARY KEY ((game_id))
);


-- =========================================================
-- ACTIVE ROOM LOOKUP
--
-- Query:
-- Check whether a player is already in an active room
--
-- Partition Key:
-- player_id
-- =========================================================

CREATE TABLE IF NOT EXISTS active_room_by_player (
         player_id uuid,
         room_code text,
         PRIMARY KEY ((player_id))
);


-- =========================================================
-- LEADERBOARD
--
-- Query:
-- Get players ordered by leaderboard ranking
--
-- Partition Key:
-- leaderboard_key
--
-- Every row uses:
-- leaderboard_key = 'GLOBAL'
--
-- Clustering order:
-- 1. Higher score first
-- 2. More games played
-- 3. More wins
-- 4. player_id for uniqueness
--
-- score_scaled:
-- displayed score * 10,000
--
-- Example:
-- 72.4538 -> 724538
-- =========================================================

CREATE TABLE IF NOT EXISTS leaderboard (
leaderboard_key text,
score_scaled bigint,
games_played int,
wins int,
player_id uuid,
player_name text,
losses int,
draws int,

PRIMARY KEY (
   (leaderboard_key),
    score_scaled,
    games_played,
    wins,
    player_id
   )
)
WITH CLUSTERING ORDER BY (
score_scaled DESC,
games_played DESC,
wins DESC,
player_id ASC
);