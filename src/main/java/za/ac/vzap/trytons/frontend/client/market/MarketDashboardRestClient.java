package za.ac.vzap.trytons.frontend.client.market;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import za.ac.vzap.trytons.frontend.client.shared.APIClient;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent
public class MarketDashboardRestClient {

    private static final String DASHBOARD_PATH = "/market-dashboard";

    private static final Logger LOG = Logger.getLogger(MarketDashboardRestClient.class.getName());

    @Inject
    private APIClient apiClient;

    public Optional<MarketDashboardResponse> getDashboard() {
        Optional<MarketDashboardResponse> response = apiClient.get(DASHBOARD_PATH, MarketDashboardResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to load market dashboard.");
        }
        return response;
    }
}
