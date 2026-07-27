package za.ac.vzap.trytons.frontend.client.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class DirectMessageResponse {
    private UUID messageId;
    private UUID senderUserId;
    private UUID recipientUserId;
    private String body;
    private LocalDateTime createdAt;
    @JsonProperty("isRead")
    private boolean isRead;
    @JsonProperty("mine")
    private boolean mine;
    // APPROVED | REJECTED. A normal conversation only ever contains APPROVED
    // messages (the backend filters REJECTED ones out); REJECTED entries only
    // ever appear in the admin's bounded moderation window.
    private String status;
}
