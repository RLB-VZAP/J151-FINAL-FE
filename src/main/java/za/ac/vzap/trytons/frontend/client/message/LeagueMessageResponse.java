package za.ac.vzap.trytons.frontend.client.message;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class LeagueMessageResponse {
    private UUID messageId;
    private UUID leagueId;
    private UUID senderUserId;
    private String senderUsername;
    private String body;
    private String status;
    private LocalDateTime createdAt;
}
