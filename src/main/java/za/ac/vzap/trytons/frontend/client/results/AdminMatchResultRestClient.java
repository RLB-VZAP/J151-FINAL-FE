package za.ac.vzap.trytons.frontend.client.results;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import za.ac.vzap.trytons.frontend.client.shared.APIClient;

@Dependent
public class AdminMatchResultRestClient {
    // Backend routes (W4-FE-FIXES-11 fix): MatchResultResource is @Path("/match-results") with
    // POST / (fixtureId travels inside MatchResultRequestDTO's body, not the URL) and
    // GET /fixture/{fixtureId}; PlayerStatisticsResource is @Path("/player-statistics") with
    // POST / (resultId/teamId/playerId travel inside PlayerStatisticsRequestDTO's body).
    private String SUBMIT_MATCH_RESULT = "/match-results";
    private String GET_MATCH_RESULT = "/match-results/fixture";
    private String SUBMIT_PLAYER_RESULT = "/player-statistics";

    private static final Logger LOG = Logger.getLogger(AdminMatchResultRestClient.class.getName());
    @Inject
    private APIClient apiClient ;

    public Optional<MatchResultResponse> submitMatchResult(String fixtureId, MatchResultRequest request){
        if(fixtureId == null || fixtureId.isBlank() || request == null){
            LOG.log(Level.WARNING, "Fixture id and match result are required to submit a match result.");
            return Optional.empty();
        }
        Optional<MatchResultResponse> response = apiClient.post(SUBMIT_MATCH_RESULT, request, MatchResultResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, "Unable to submit match result");
        }
        return response;
    }

    public Optional<PlayerStatisticsResponse> submitPlayerStatistics(String fixtureId, PlayerStatisticsRequest request){
        if(fixtureId == null || fixtureId.isBlank() || request == null){
            LOG.log(Level.WARNING, "Fixture id and player statistics are required to submit player statistics.");
            return Optional.empty();
        }
        Optional<PlayerStatisticsResponse> response = apiClient.post(SUBMIT_PLAYER_RESULT, request, PlayerStatisticsResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, "Unable to submit player statistics");
        }
        return response;
    }

    public Optional<MatchResultResponse> getMatchResult(String fixtureId){
        if(fixtureId == null || fixtureId.isBlank()){
            LOG.log(Level.WARNING, "Fixture id is required to get a match result.");
            return Optional.empty();
        }
        String path = GET_MATCH_RESULT + "/" + encode(fixtureId);
        Optional<MatchResultResponse> response = apiClient.get(path, MatchResultResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, "Unable to get match result");
        }
        return response;
    }

    public String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
