package za.ac.vzap.trytons.frontend.client.fixture;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import za.ac.vzap.trytons.frontend.client.shared.APIClient;

@Dependent
public class FixtureRestClient {
    private static final String FIXTURE_PATH = "/fixtures";
    private static final Logger LOG = Logger.getLogger(FixtureRestClient.class.getName());

    @Inject
    private APIClient apiClient;

    public Optional<List<FixtureResponse>> listFixtures(String statusFilter) {
        String path = FIXTURE_PATH;
        if(statusFilter != null && !statusFilter.isBlank()) {
            path += "?status=" + encode(statusFilter.trim());
        }
        Optional<FixtureResponse[]> response = apiClient.get(path, FixtureResponse[].class);
        if(response.isEmpty()){
            LOG.log(Level.WARNING, "Unable list fixtures");
        }
        return response.map(fixtures -> new ArrayList<>(Arrays.asList(fixtures)));
    }

    /**
     * League-scoped fixtures — pool, knockout and any non-tournament round,
     * keyed on the league rather than a tournament so it also works for a
     * league that has never been started (no tournament exists yet).
     */
    public Optional<List<FixtureResponse>> listFixturesForLeague(String leagueId, String statusFilter) {
        StringBuilder path = new StringBuilder(FIXTURE_PATH).append("?leagueId=").append(encode(leagueId));
        if (statusFilter != null && !statusFilter.isBlank()) {
            path.append("&status=").append(encode(statusFilter.trim()));
        }
        Optional<FixtureResponse[]> response = apiClient.get(path.toString(), FixtureResponse[].class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to list fixtures for league");
        }
        return response.map(fixtures -> new ArrayList<>(Arrays.asList(fixtures)));
    }

    public Optional<FixtureResponse> getFixture(String fixtureId) {
        String path =FIXTURE_PATH + "/" + encode(fixtureId);
        Optional<FixtureResponse> response = apiClient.get(path,FixtureResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.WARNING, "Unable find fixture");
        }
        return response;
    }

    public String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
