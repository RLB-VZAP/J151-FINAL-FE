package za.ac.vzap.trytons.frontend.client;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent
public class PlayerRestClient {

    private static final String PLAYER_PATH = "/player";
    private static final Logger LOG = Logger.getLogger(PlayerRestClient.class.getName());

    @Inject
    private APIClient apiClient;

    public Optional<List<PlayerResponse>> listPlayers(String search, UUID clubId, UUID positionId) {
        List<String> parameters = new ArrayList<>();

        if (search != null && !search.isBlank()) {
            parameters.add("search=" + encode(search.trim()));
        }
        if (clubId != null) {
            parameters.add("clubId=" + clubId);
        }
        if (positionId != null) {
            parameters.add("positionId=" + positionId);
        }

        String path = PLAYER_PATH;
        if (!parameters.isEmpty()) {
            path += "?" + String.join("&", parameters);
        }

        Optional<PlayerResponse[]> response = apiClient.get(path, PlayerResponse[].class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to list players.");
        }
        return response.map(players -> new ArrayList<>(Arrays.asList(players)));
    }

    public Optional<PlayerResponse> getPlayer(UUID playerId) {
        return apiClient.get(PLAYER_PATH + "/" + playerId, PlayerResponse.class);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
