package com.svi.tictactoe.controller;

import com.svi.tictactoe.dto.response.leaderboard.LeaderboardResponse;
import com.svi.tictactoe.service.LeaderboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/leaderboard")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping
    public ResponseEntity<LeaderboardResponse> getLeaderboard() {
        return ResponseEntity.ok(leaderboardService.getLeaderboard());
    }
}