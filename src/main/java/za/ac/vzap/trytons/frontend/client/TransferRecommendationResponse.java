package za.ac.vzap.trytons.frontend.client;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class TransferRecommendationResponse {
    private List<RecommendedPlayer> recommendation;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecommendedPlayer {
        private UUID playerId;
        private String playerName;
        private BigDecimal value;
        private String reason;
    }
}
