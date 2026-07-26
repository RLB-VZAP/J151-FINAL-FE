package za.ac.vzap.trytons.frontend.client.message;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ConversationThreadResponse {
    private UUID counterpartUserId;
    private String counterpartUsername;
    private String lastMessageBody;
    private LocalDateTime lastMessageAt;
    private int unreadCount;
}
