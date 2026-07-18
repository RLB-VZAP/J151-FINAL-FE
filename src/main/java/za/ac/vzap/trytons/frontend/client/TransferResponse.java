package za.ac.vzap.trytons.frontend.client;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class TransferResponse {
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
    private int penaltyPoints;

    private String status;
    private String transferDate;
    private String confirmationDate;
}
