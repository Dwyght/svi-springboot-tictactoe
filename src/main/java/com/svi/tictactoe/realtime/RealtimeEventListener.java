package com.svi.tictactoe.realtime;

import com.svi.tictactoe.constant.RealtimeTopic;
import com.svi.tictactoe.event.GameChangedEvent;
import com.svi.tictactoe.event.LeaderboardChangedEvent;
import com.svi.tictactoe.event.RoomChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class RealtimeEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    public RealtimeEventListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleRoomChanged(RoomChangedEvent event) {
        messagingTemplate.convertAndSend(RealtimeTopic.room(event.room().roomCode()), event.room());
    }

    @EventListener
    public void handleGameChanged(GameChangedEvent event) {
        messagingTemplate.convertAndSend(RealtimeTopic.game(event.game().gameId()), event.game());
    }

    @EventListener
    public void handleLeaderboardChanged(LeaderboardChangedEvent event) {
        messagingTemplate.convertAndSend(RealtimeTopic.LEADERBOARD, event.leaderboard());
    }
}