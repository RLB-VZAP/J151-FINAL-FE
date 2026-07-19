package za.ac.vzap.trytons.frontend.client.leaderboard;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.GenericType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import za.ac.vzap.trytons.frontend.client.shared.APIClient;

@Dependent
public class LeaderboardRestClient { // check the backend endpoints again to get master leaderboard
    private static final String GET_LEADERBOARD_FOR_LEAGUE_PATH = "/leaderboard";
    private static final String GET_RANKING_FOR_TEAM_PATH = "/leaderboard";

    private static final Logger LOG = Logger.getLogger(LeaderboardRestClient.class.getName());

    @Inject
    public APIClient apiClient;

    public Optional<List<LeaderboardEntryResponse>> getLeaderboardForLeague(UUID leagueId) {
        String path = GET_LEADERBOARD_FOR_LEAGUE_PATH + "/" + leagueId + "/rankings";
        Optional<List<LeaderboardEntryResponse>> response = apiClient.getList(path, new GenericType<List<LeaderboardEntryResponse>>(){});
        if (response.isEmpty()){
            LOG.log(Level.SEVERE, "Could not get leaderboard for league.");
        }
        return response;
    }

    public Optional<LeaderboardEntryResponse> getRankingForTeam(UUID teamId, UUID leaderboardId) {
        // TODO [W4-FE-FIXES-17]: teamId/leaderboardId concatenated into URL unencoded — encode per
        //   PlayerRestClient.listPlayers pattern (see W4-CR-FE-08)
        String path = GET_RANKING_FOR_TEAM_PATH + "/team" + "/" + teamId + "?leaderboardId=" + leaderboardId;
        Optional<LeaderboardEntryResponse> response = apiClient.get(path, LeaderboardEntryResponse.class);
        if (response.isEmpty()){
            LOG.log(Level.SEVERE, "Could not get ranking for team.");
        }
        return response;
    }


}
