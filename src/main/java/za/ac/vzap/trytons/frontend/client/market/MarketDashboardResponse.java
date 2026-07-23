package za.ac.vzap.trytons.frontend.client.market;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MarketDashboardResponse {
    private List<MarketPlayerResponse> trending;
    private List<MarketPlayerResponse> mostTransferredIn;
    private List<MarketPlayerResponse> mostTransferredOut;
    private List<MarketPlayerResponse> hiddenGems;
    private List<MarketPlayerResponse> overpriced;
    private List<MarketPlayerResponse> popularCaptains;
}
