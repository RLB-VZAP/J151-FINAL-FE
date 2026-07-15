package za.ac.vzap.trytons.frontend.client;

import jakarta.json.bind.annotation.JsonbProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransferResponse {
    private String transferId;
    private String teamId;
    private String roundId;

    @JsonbProperty("removed_player_id")
    private String removedPlayerId;
    @JsonbProperty("removed_player_name")
    private String removedPlayerName;
    @JsonbProperty("removed_player_value")
    private BigDecimal removedPlayerValue;

    @JsonbProperty("added_player_id")
    private String addedPlayerId;
    @JsonbProperty("added_player_name")
    private String addedPlayerName;
    @JsonbProperty("added_player_value")
    private BigDecimal addedPlayerValue;

    private BigDecimal valueDifference;
    private Integer penaltyPoints;
    public boolean isPenaltyApplied(){
        return penaltyPoints != null && penaltyPoints > 0;
    }

    private BigDecimal newRemainingBudget;
    private BigDecimal newTotalTeamValue;

    private String status;
    private String transferWindowStatus;
    private String transferDate;
    private String confirmationDate;
    private String confirmedAt;
    private String message;
}