package za.ac.vzap.trytons.frontend.client;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class TransferRecommendationResponse {

    // TODO [DTO-ALIGNMENT]: Align with TransferRecommendationResponseDTO: use teamId plus
    //   `recommendations` (plural). Its RecommendedPlayerDTO also includes positionName, clubName,
    //   currentForm, availabilityStatus, and replacesPlayerId, all missing from this nested model.

    private List<RecommendedPlayer> recommendation;

    public TransferRecommendationResponse() {
    }

    public TransferRecommendationResponse(List<RecommendedPlayer> recommendation) {
        this.recommendation = recommendation;
    }

    public List<RecommendedPlayer> getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(List<RecommendedPlayer> recommendation) {
        this.recommendation = recommendation;
    }

    public static class RecommendedPlayer {
        private UUID playerId;
        private String playerName;
        private BigDecimal value;
        private String reason;

        public RecommendedPlayer() {
        }

        public RecommendedPlayer(UUID playerId, String playerName, BigDecimal value, String reason) {
            this.playerId = playerId;
            this.playerName = playerName;
            this.value = value;
            this.reason = reason;
        }

        public UUID getPlayerId() {
            return playerId;
        }

        public void setPlayerId(UUID playerId) {
            this.playerId = playerId;
        }

        public String getPlayerName() {
            return playerName;
        }

        public void setPlayerName(String playerName) {
            this.playerName = playerName;
        }

        public BigDecimal getValue() {
            return value;
        }

        public void setValue(BigDecimal value) {
            this.value = value;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
