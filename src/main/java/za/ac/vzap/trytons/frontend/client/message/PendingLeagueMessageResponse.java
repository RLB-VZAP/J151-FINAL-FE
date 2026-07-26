package za.ac.vzap.trytons.frontend.client.message;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class PendingLeagueMessageResponse {
    private UUID messageId;
    private UUID leagueId;
    private String leagueName;
    private UUID senderUserId;
    private String senderUsername;
    private String body;
    private String flaggedReason;
    private LocalDateTime createdAt;
}
