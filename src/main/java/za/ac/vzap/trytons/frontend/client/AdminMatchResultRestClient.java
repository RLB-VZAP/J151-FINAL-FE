package za.ac.vzap.trytons.frontend.client;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent
public class AdminMatchResultRestClient {
    // TODO [W4-FE-FIXES-11]: wrong backend routes — client targets /match_result/{fixtureId} and
    //   /match_player/{fixtureId} (constants lines 12-14); backend exposes /match-results
    //   (POST /, GET /fixture/{fixtureId}) and /player-statistics — every call 404s (see W4-CR-FE-07)
    private String SUBMIT_MATCH_RESULT = "/match_result";
    private String GET_MATCH_RESULT = "/match_result";
    private String SUBMIT_PLAYER_RESULT = "/match_player";

    private static final Logger LOG = Logger.getLogger(AdminMatchResultRestClient.class.getName());
    @Inject
    private APIClient apiClient ;

    public Optional<MatchResultResponse> submitMatchResult(String fixtureId, MatchResultRequest request){
        if(fixtureId == null || fixtureId.isBlank() || request == null){
            LOG.log(Level.WARNING, "Fixture id and match result are required to submit a match result.");
            return Optional.empty();
        }
        String path = SUBMIT_MATCH_RESULT + "/" + fixtureId;
        Optional<MatchResultResponse> response = apiClient.post(path, request, MatchResultResponse.class);
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
        String path = SUBMIT_PLAYER_RESULT + "/" + fixtureId;
        Optional<PlayerStatisticsResponse> response = apiClient.post(path, request, PlayerStatisticsResponse.class);
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
        String path = GET_MATCH_RESULT + "/" + fixtureId;
        Optional<MatchResultResponse> response = apiClient.get(path, MatchResultResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, "Unable to get match result");
        }
        return response;
    }
}
