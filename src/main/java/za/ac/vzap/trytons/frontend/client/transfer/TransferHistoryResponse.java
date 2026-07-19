package za.ac.vzap.trytons.frontend.client.transfer;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class TransferHistoryResponse {
    private UUID transferId;
    private UUID teamId;
    private UUID roundId;
    private UUID removedPlayerId;
    private String removedPlayerName;
    private BigDecimal removedPlayerValue;
    private UUID addedPlayerId;
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
