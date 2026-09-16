package com.svi.tictactoe.controller;

import com.svi.tictactoe.dto.response.leaderboard.LeaderboardResponse;
import com.svi.tictactoe.service.LeaderboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Exposes the REST endpoint for reading the global player leaderboard.
 */
@RestController
@RequestMapping("/api/v1/leaderboard")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    /**
     * Retrieves players in their current leaderboard order.
     *
     * @return a response containing the leaderboard with status 200 OK
     */
    @GetMapping
    public ResponseEntity<LeaderboardResponse> getLeaderboard() {
        return ResponseEntity.ok(leaderboardService.getLeaderboard());
    }
}
