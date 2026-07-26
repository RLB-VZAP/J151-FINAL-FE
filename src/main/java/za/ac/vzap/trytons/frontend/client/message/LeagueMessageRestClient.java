package za.ac.vzap.trytons.frontend.client.message;

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
public class LeagueMessageRestClient {

    private static final Logger LOG = Logger.getLogger(LeagueMessageRestClient.class.getName());

    @Inject
    private APIClient apiClient;

    public Optional<List<LeagueMessageResponse>> getFeed(String leagueId, String since) {
        if (isBlank(leagueId)) {
            LOG.log(Level.WARNING, "A league id is required to load the chat feed.");
            return Optional.empty();
        }
        String path = "/leagues/" + encode(leagueId) + "/messages";
        if (since != null && !since.isBlank()) {
            path += "?since=" + encode(since);
        }
        Optional<LeagueMessageResponse[]> response = apiClient.get(path, LeagueMessageResponse[].class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to load league chat feed.");
        }
        return response.map(messages -> new ArrayList<>(Arrays.asList(messages)));
    }

    public Optional<LeagueMessageResponse> post(String leagueId, SendLeagueMessageRequest request) {
        if (isBlank(leagueId) || request == null) {
            LOG.log(Level.WARNING, "A league id and message are required.");
            return Optional.empty();
        }
        String path = "/leagues/" + encode(leagueId) + "/messages";
        Optional<LeagueMessageResponse> response = apiClient.post(path, request, LeagueMessageResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to post league message.");
        }
        return response;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
