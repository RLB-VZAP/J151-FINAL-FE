package za.ac.vzap.trytons.frontend.client.pricing;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class PriceChangeResponse {
    private UUID playerId;
    private String playerName;
    private BigDecimal oldValue;
    private BigDecimal newValue;
    private BigDecimal delta;
}
