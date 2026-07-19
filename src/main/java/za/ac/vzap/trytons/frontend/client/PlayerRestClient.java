package za.ac.vzap.trytons.frontend.client;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent
public class PlayerRestClient {
    private String LIST_PLAYERS ="/player";
    private String GET_PLAYER = "/player";
    private String CREATE_PLAYER = "/player";
    private String UPDATE_PLAYER ="/player";

    private static final Logger LOG = Logger.getLogger(PlayerRestClient.class.getName());
    @Inject
    private APIClient apiClient;

    public Optional<List<PlayerResponse>> listPlayers(String search , UUID clubId , UUID positionId) {
        StringBuilder path = new StringBuilder(LIST_PLAYERS);
        List<String> params = new ArrayList<>();
        Optional<PlayerResponse[]> response = apiClient.get(path.toString(), PlayerResponse[].class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, "Unable list player");
        }
        return response.map(players -> new ArrayList<>(Arrays.asList(players)));
    }

    public Optional<PlayerResponse> getPlayer(UUID playerId) {
        String path = GET_PLAYER + "/" + playerId;
        Optional<PlayerResponse> response = apiClient.get(path,PlayerResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, "Unable find player");
        }
        return response;
    }

    public Optional<PlayerResponse> createPlayer(PlayerRequest request) {
        Optional<PlayerResponse> response = apiClient.post(CREATE_PLAYER,request,PlayerResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, "Unable to create player");
        }
        return response;
    }

    public Optional<PlayerResponse> updatePlayer (UUID playerId , PlayerRequest request) {
        String path = UPDATE_PLAYER + "/" + playerId;
        Optional<PlayerResponse> response = apiClient.put(path,request,PlayerResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, "Unable to update player");
        }
        return response;
    }
}
