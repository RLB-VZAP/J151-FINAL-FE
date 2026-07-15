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
public class ClubRestClient {

    private static final String CLUB_PATH = "/club";
    private static final Logger LOG = Logger.getLogger(ClubRestClient.class.getName());

    @Inject
    private APIClient apiClient;

    public Optional<List<ClubResponse>> listClubs() {
        Optional<ClubResponse[]> response = apiClient.get(CLUB_PATH, ClubResponse[].class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to load clubs.");
        }
        return response.map(clubs -> new ArrayList<>(Arrays.asList(clubs)));
    }

    public Optional<ClubResponse> getClubById(UUID clubId) {
        Optional<ClubResponse> response = apiClient.get(CLUB_PATH + "/" + clubId, ClubResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to find club {0}.", clubId);
        }
        return response;
    }

}
