package za.ac.vzap.trytons.frontend.client.pricing;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PricingRunSummaryResponse {
    private LocalDateTime runAt;
    private boolean applied;
    private int playersEvaluated;
    private int playersRepriced;
    private BigDecimal totalIncrease;
    private BigDecimal totalDecrease;
    private List<PriceChangeResponse> changes;
}
