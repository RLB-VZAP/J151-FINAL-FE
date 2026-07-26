package za.ac.vzap.trytons.frontend.client.pricing;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import za.ac.vzap.trytons.frontend.client.shared.APIClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent
public class PricingRestClient {

    private static final String SETTINGS_PATH = "/admin/pricing/settings";
    private static final String PREVIEW_PATH = "/admin/pricing/preview";
    private static final String RUN_PATH = "/admin/pricing/run";

    private static final Logger LOG = Logger.getLogger(PricingRestClient.class.getName());

    @Inject
    private APIClient apiClient;

    public Optional<PricingSettingsResponse> getSettings() {
        Optional<PricingSettingsResponse> response = apiClient.get(SETTINGS_PATH, PricingSettingsResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to load pricing settings.");
        }
        return response;
    }

    public Optional<PricingSettingsResponse> updateSettings(PricingSettingsResponse request) {
        if (request == null) {
            return Optional.empty();
        }
        Optional<PricingSettingsResponse> response = apiClient.put(SETTINGS_PATH, request, PricingSettingsResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to update pricing settings.");
        }
        return response;
    }

    public Optional<PricingRunSummaryResponse> preview() {
        Optional<PricingRunSummaryResponse> response = apiClient.get(PREVIEW_PATH, PricingRunSummaryResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to preview pricing run.");
        }
        return response;
    }

    public Optional<PricingRunSummaryResponse> run() {
        Optional<PricingRunSummaryResponse> response = apiClient.post(RUN_PATH, null, PricingRunSummaryResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to run pricing update.");
        }
        return response;
    }

    public Optional<List<PlayerPriceHistoryResponse>> getPlayerHistory(String playerId, int limit) {
        if (playerId == null || playerId.isBlank()) {
            return Optional.empty();
        }
        String path = "/player-prices/" + encode(playerId) + "/history?limit=" + limit;
        Optional<PlayerPriceHistoryResponse[]> response = apiClient.get(path, PlayerPriceHistoryResponse[].class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to load player price history.");
        }
        return response.map(rows -> new ArrayList<>(Arrays.asList(rows)));
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
