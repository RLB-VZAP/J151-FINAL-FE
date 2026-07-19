package za.ac.vzap.trytons.frontend.client.fantasyteam;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import za.ac.vzap.trytons.frontend.client.shared.APIClient;

@Dependent
public class FantasyTeamRestClient {
    private static final String FANTASY_TEAM_PATH = "/fantasy-team";
    private static final String OWN_TEAM_PATH = "/own";
    private static final String OPPONENT_TEAM_PATH = "/opponent";
    private static final Logger LOG = Logger.getLogger(FantasyTeamRestClient.class.getName());

    @Inject
    private APIClient apiClient;
    public Optional<FantasyTeamResponse> createTeam(FantasyTeamRequest request){
        Optional<FantasyTeamResponse> response = apiClient.post(FANTASY_TEAM_PATH,request,FantasyTeamResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.WARNING, "Unable to create fantasy team.");
        }
        return response;
    }

    public Optional<ViewOwnTeamResponse> viewOwnTeam(String teamName){
        String path = FANTASY_TEAM_PATH + OWN_TEAM_PATH + "/" + teamName;
        Optional<ViewOwnTeamResponse> response = apiClient.get(path, ViewOwnTeamResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.WARNING, "Unable to view own fantasy team.");
        }
        return response;
    }
    public Optional<ViewOpponentTeamResponse> viewOpponentTeam(UUID teamId){
        String path = FANTASY_TEAM_PATH + OPPONENT_TEAM_PATH + "/" + teamId;
        Optional<ViewOpponentTeamResponse> response = apiClient.get(path, ViewOpponentTeamResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.WARNING, "Unable to view opponent fantasy team.");
        }
        return response;
    }

    public Optional<FantasyTeamResponse> updateTeam(UUID teamId, FantasyTeamRequest request){
        String path = FANTASY_TEAM_PATH + "/" + teamId;
        Optional<FantasyTeamResponse> response = apiClient.post(path, request,FantasyTeamResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.WARNING, "Unable to update fantasy team.");
        }
        return response;
    }
}
