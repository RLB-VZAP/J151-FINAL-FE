package za.ac.vzap.trytons.frontend.client.pricing;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PlayerPriceHistoryResponse {
    private BigDecimal oldValue;
    private BigDecimal newValue;
    private BigDecimal delta;
    private String reason;
    private LocalDateTime createdAt;
}
