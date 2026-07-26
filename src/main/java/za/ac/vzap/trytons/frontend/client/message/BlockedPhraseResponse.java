package za.ac.vzap.trytons.frontend.client.message;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class BlockedPhraseResponse {
    private UUID blocklistId;
    private String phrase;
    private LocalDateTime createdAt;
}
