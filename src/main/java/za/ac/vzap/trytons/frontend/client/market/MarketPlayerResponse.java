package za.ac.vzap.trytons.frontend.client.market;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class MarketPlayerResponse {
    private UUID playerId;
    private String playerName;
    private BigDecimal value;
    private int transfersIn;
    private int transfersOut;
    private int netTransfers;
    private int ownershipCount;
    private int recentPoints;
    private BigDecimal pointsPerValue;
    private int captainCount;
}
