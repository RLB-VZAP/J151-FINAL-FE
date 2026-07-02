package za.ac.vzap.trytons.frontend.client;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent
public class LeaderboardRestClient {
    private WebTarget target;
    private Client client;

    private final String baseUrl = "leaderboard";
    private String GET_LEADERBOARD_FOR_LEAGUE_PATH = baseUrl + "/{leagueId}/rankings";
    private String GET_RANKING_FOR_TEAM_PATH = baseUrl + "/team/{teamId}";

    ObjectMapper mapper = new ObjectMapper();
    private static final Logger LOG = Logger.getLogger(LeaderboardRestClient.class.getName());

    @Inject
    public APIClient apiClient;

    public Optional<LeaderboardEntryResponse> getLeaderboardForLeague(){
        Optional<LeaderboardEntryResponse> response = apiClient.get(GET_LEADERBOARD_FOR_LEAGUE_PATH, LeaderboardEntryResponse.class);
        if (response.isEmpty()){
            LOG.log(Level.SEVERE, "Could not get leaderboard for league.");
        }
        return response;
    }

    public Optional<LeaderboardEntryResponse> getRankingForTeam(){
        Optional<LeaderboardEntryResponse> response = apiClient.get(GET_RANKING_FOR_TEAM_PATH, LeaderboardEntryResponse.class);
        if (response.isEmpty()){
            LOG.log(Level.SEVERE, "Could not get ranking for team.");
        }
        return response;
    }


}
