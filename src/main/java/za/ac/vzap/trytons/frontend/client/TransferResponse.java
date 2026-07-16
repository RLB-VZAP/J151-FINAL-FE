package za.ac.vzap.trytons.frontend.client;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransferResponse {
    private String transferId;
    private String teamId;
    private String roundId;

    // TODO [W4-FE-FIXES-25]: DTO field-casing drift — backend TransferResponseDTO serializes
    //   snake_case keys (removed_player_id, added_player_id, ...) but these fields are camelCase, so
    //   the removed/added player fields never populate from executeTransfer. It also declares no
    //   penaltyApplied, remaining-budget, total-value, transfer-window, confirmedAt, or message fields;
    //   align the model or add @JsonProperty (distinct from the envelope mismatch FIXES-26).
    private String removedPlayerId;
    private String removedPlayerName;
    private BigDecimal removedPlayerValue;

    private String addedPlayerId;
    private String addedPlayerName;
    private BigDecimal addedPlayerValue;

    private BigDecimal valueDifference;
    private Integer penaltyPoints;
    private Boolean penaltyApplied;

    private BigDecimal newRemainingBudget;
    private BigDecimal newTotalTeamValue;

    private String status;
    private String transferWindowStatus;
    private String transferDate;
    private String confirmationDate;
    private String confirmedAt;
    private String message;
}
