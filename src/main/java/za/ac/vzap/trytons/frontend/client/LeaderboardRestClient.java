package za.ac.vzap.trytons.frontend.client;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent
public class LeaderboardRestClient {
    private String GET_LEADERBOARD_FOR_LEAGUE_PATH = "/leaderboard";
    private String GET_RANKING_FOR_TEAM_PATH = "/leaderboard";

    private static final Logger LOG = Logger.getLogger(LeaderboardRestClient.class.getName());

    @Inject
    public APIClient apiClient;

    public Optional<LeaderboardEntryResponse> getLeaderboardForLeague(UUID leagueId) {
        String path = GET_LEADERBOARD_FOR_LEAGUE_PATH + "/" + leagueId + "/rankings";
        Optional<LeaderboardEntryResponse> response = apiClient.get(path, LeaderboardEntryResponse.class);
        if (response.isEmpty()){
            LOG.log(Level.SEVERE, "Could not get leaderboard for league.");
        }
        return response;
    }

    public Optional<LeaderboardEntryResponse> getRankingForTeam(UUID teamId, UUID leaderboardId) {
        String path = GET_RANKING_FOR_TEAM_PATH + "/team" + "/" + teamId + "?leaderboardId=" + leaderboardId;
        Optional<LeaderboardEntryResponse> response = apiClient.get(path, LeaderboardEntryResponse.class);
        if (response.isEmpty()){
            LOG.log(Level.SEVERE, "Could not get ranking for team.");
        }
        return response;
    }


}
