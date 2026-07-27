package za.ac.vzap.trytons.frontend.client.message;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class MessageRequestResponse {
    private UUID requestId;

    private UUID requesterUserId;
    private String requesterUsername;

    private UUID targetUserId;
    private String targetUsername;

    // PENDING | APPROVED | REJECTED
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
}
