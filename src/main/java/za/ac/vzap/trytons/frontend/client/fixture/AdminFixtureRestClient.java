package za.ac.vzap.trytons.frontend.client.fixture;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import za.ac.vzap.trytons.frontend.client.results.MatchProcessingResponse;
import za.ac.vzap.trytons.frontend.client.results.MatchResultResponse;
import za.ac.vzap.trytons.frontend.client.shared.APIClient;

@Dependent
public class AdminFixtureRestClient {
    private static final String FIXTURES_PATH = "/fixtures";
    private static final String SIMULATIONS_PATH = "/simulations/fixtures";
    private static final String MATCH_PROCESSING_PATH = "/match-processing/fixtures";

    private static final Logger LOG = Logger.getLogger(AdminFixtureRestClient.class.getName());
    @Inject
    private APIClient apiClient ;

    /*
        Simulating and processing are NOT status updates.

        COMPLETED and PROCESSED are derived facts — "this match was played" and
        "its points, team scores and leaderboards are up to date". The backend no
        longer lets PUT /fixtures/{id}/status assert either of them, because doing
        so wrote the status and none of the rows behind it. These two calls invoke
        the services that actually do the work.
    */
    public Optional<MatchResultResponse> simulateFixture(String fixtureId){
        String path = SIMULATIONS_PATH + "/" + encode(fixtureId);
        Optional<MatchResultResponse> response = apiClient.post(path, null, MatchResultResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.WARNING, "Unable to simulate fixture {0}", fixtureId);
        }
        return response;
    }

    public Optional<MatchProcessingResponse> processFixture(String fixtureId){
        String path = MATCH_PROCESSING_PATH + "/" + encode(fixtureId);
        Optional<MatchProcessingResponse> response = apiClient.post(path, null, MatchProcessingResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.WARNING, "Unable to process fixture {0}", fixtureId);
        }
        return response;
    }

    // No create: the backend has no POST /fixtures. Fixtures are generated when a
    // tournament is created, so administrators may only view and update them.
    public Optional<FixtureResponse> updateFixtureStatus(String fixtureId, String status){
        String path = FIXTURES_PATH + "/" + encode(fixtureId) + "/status?status=" + encode(status);
        Optional<FixtureResponse> response = apiClient.put(path, null, FixtureResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, "Unable to update fixture");
        }
        return response;
    }

    public Optional<List<FixtureResponse>> listFixtures(){
        String path = FIXTURES_PATH;

        Optional<FixtureResponse[]> response = apiClient.get(path, FixtureResponse[].class);
        if(response.isEmpty()){
            LOG.log(Level.WARNING, "Unable list fixtures");
        }
        return response.map(fixtures -> new ArrayList<>(Arrays.asList(fixtures)));

    }

    public String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

}


