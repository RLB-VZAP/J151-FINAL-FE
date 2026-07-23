package za.ac.vzap.trytons.frontend.client.pricing;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Used for both reading (GET) and updating (PUT body) the pricing weights/bounds. */
@Getter
@Setter
@NoArgsConstructor
public class PricingSettingsResponse {
    private BigDecimal weightForm;
    private BigDecimal weightPopularity;
    private BigDecimal weightPoints;
    private BigDecimal weightInjury;
    private BigDecimal weightDemand;
    private BigDecimal weightAvailability;

    private BigDecimal maxDeltaPct;
    private BigDecimal minValue;
    private BigDecimal maxValue;
}
