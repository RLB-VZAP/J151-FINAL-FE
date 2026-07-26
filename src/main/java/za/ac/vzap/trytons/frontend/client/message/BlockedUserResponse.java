package za.ac.vzap.trytons.frontend.client.message;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class BlockedUserResponse {
    private UUID userId;
    private String username;
    private LocalDateTime blockedAt;
}
