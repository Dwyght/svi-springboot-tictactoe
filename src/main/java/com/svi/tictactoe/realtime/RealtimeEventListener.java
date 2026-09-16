package com.svi.tictactoe.realtime;

import com.svi.tictactoe.constant.RealtimeTopic;
import com.svi.tictactoe.event.GameChangedEvent;
import com.svi.tictactoe.event.LeaderboardChangedEvent;
import com.svi.tictactoe.event.RoomChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Translates room, game, and leaderboard application events into STOMP topic broadcasts.
 */
@Component
public class RealtimeEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    public RealtimeEventListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Broadcasts an updated room to subscribers of that room's topic.
     *
     * @param event the room change event
     */
    @EventListener
    public void handleRoomChanged(RoomChangedEvent event) {
        messagingTemplate.convertAndSend(RealtimeTopic.room(event.room().roomCode()), event.room());
    }

    /**
     * Broadcasts an updated game to subscribers of that game's topic.
     *
     * @param event the game change event
     */
    @EventListener
    public void handleGameChanged(GameChangedEvent event) {
        messagingTemplate.convertAndSend(RealtimeTopic.game(event.game().gameId()), event.game());
    }

    /**
     * Broadcasts the updated global leaderboard to leaderboard subscribers.
     *
     * @param event the leaderboard change event
     */
    @EventListener
    public void handleLeaderboardChanged(LeaderboardChangedEvent event) {
        messagingTemplate.convertAndSend(RealtimeTopic.LEADERBOARD, event.leaderboard());
    }
}
