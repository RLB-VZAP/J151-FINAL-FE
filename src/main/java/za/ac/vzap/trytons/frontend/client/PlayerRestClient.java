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

        // TODO [W4-FE-FIXES-23]: BLOCKED BY BACKEND — PlayerResource @Path("/{player}") and
        //   LockStatusResource @Path("/{lock-status}") are catch-all templates (not literals), so
        //   /player and /player/{id} can dispatch to the lock-status stub and 501; tripwire at the FE
        //   call site, fix is backend-side literal @Path values (see W4-CR-BE-05)
        // TODO [DTO-ALIGNMENT]: PlayerResource wraps both list and single-player responses in
        //   ApiResponseDTO. Unwrap its data field before mapping PlayerResponse values; direct DTO
        //   deserialization leaves successful player catalogue responses empty.
        Optional<PlayerResponse[]> response = apiClient.get(path, PlayerResponse[].class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to list players.");
        }
        return response.map(players -> new ArrayList<>(Arrays.asList(players)));
    }

    public Optional<PlayerResponse> getPlayer(UUID playerId) {
        // TODO [W4-FE-FIXES-23]: BLOCKED BY BACKEND — /player/{id} can dispatch to the lock-status stub
        //   (catch-all @Path); tripwire, fix is backend-side literal @Path values (see W4-CR-BE-05)
        // TODO [DTO-ALIGNMENT]: GET /player/{id} returns ApiResponseDTO, not a bare PlayerResponse;
        //   unwrap data before returning the player detail.
        return apiClient.get(PLAYER_PATH + "/" + playerId, PlayerResponse.class);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
