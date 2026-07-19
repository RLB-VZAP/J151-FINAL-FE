package za.ac.vzap.trytons.frontend.client.catalog;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import za.ac.vzap.trytons.frontend.client.shared.APIClient;
@Dependent
public class ClubRestClient {
    private String LIST_CLUBS = "/club";
    private String GET_CLUB_BY_ID = "/club";
    private String CREATE_CLUB = "/club";
    private String UPDATE_CLUB = "/club";

    private static final Logger LOG = Logger.getLogger(ClubRestClient.class.getName());

    @Inject
    private APIClient apiClient;

    public Optional<List<ClubResponse>> listClubs() {
        Optional<ClubResponse[]> response = apiClient.get(LIST_CLUBS, ClubResponse[].class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, "Unable list Clubs");
        }
        return response.map(clubs -> new ArrayList<>(Arrays.asList(clubs)));
    }

    public Optional<ClubResponse> getClubById(UUID clubId) {
        String path = GET_CLUB_BY_ID + "/" + clubId;
        Optional<ClubResponse> response = apiClient.get(path,ClubResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, "Unable find Club");
        }
        return response;
    }

    public Optional<ClubResponse> createClub(ClubRequest request){
        Optional<ClubResponse> response = apiClient.post(CREATE_CLUB,request,ClubResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, "Unable create Club");
        }
        return response;
    }

    public Optional<ClubResponse> updateClub(UUID clubId,ClubRequest request){
        String path = UPDATE_CLUB + "/" + clubId;
        Optional<ClubResponse> response = apiClient.put(path,request,ClubResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, "Unable to update Club");
        }
        return response;
    }
}
