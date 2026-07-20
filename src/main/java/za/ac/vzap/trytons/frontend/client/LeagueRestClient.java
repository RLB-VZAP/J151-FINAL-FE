package za.ac.vzap.trytons.frontend.client;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent
public class LeagueRestClient {

    private static final String LEAGUES_PATH = "/league";
    private static final String MY_LEAGUES_PATH = "/league?mine=true";
    private static final String JOIN_PATH = "/league/join";

    private static final Logger LOG = Logger.getLogger(LeagueRestClient.class.getName());

    @Inject
    private APIClient apiClient;

    public Optional<List<LeagueResponse>> listPublicLeagues(){

        Optional<LeagueResponse[]> response = apiClient.get(LEAGUES_PATH, LeagueResponse[].class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to list public leagues.");
        }
        return response.map(leagues -> new ArrayList<>(Arrays.asList(leagues)));
    }

    public Optional<List<LeagueResponse>> listMyLeagues(){

        Optional<LeagueResponse[]> response = apiClient.get(MY_LEAGUES_PATH, LeagueResponse[].class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to list my leagues.");
        }
        return response.map(leagues -> new ArrayList<>(Arrays.asList(leagues)));
    }

    public Optional<LeagueResponse> getLeague(String leagueId){

        if (isBlank(leagueId)) {
            LOG.log(Level.WARNING, "League id is required to get a league.");
            return Optional.empty();
        }

        String path = LEAGUES_PATH + "/" + leagueId;
        Optional<LeagueResponse> response = apiClient.get(path, LeagueResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to get league.");
        }
        return response;
    }

    public Optional<LeagueResponse> createLeague(LeagueRequest request){

        if (!isValidLeagueRequest(request)) {
            LOG.log(Level.WARNING, "League request is invalid.");
            return Optional.empty();
        }

        Optional<LeagueResponse> response = apiClient.post(LEAGUES_PATH, request, LeagueResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to create league.");
        }
        return response;
    }

    public Optional<LeagueResponse> joinLeague(JoinLeagueRequest request){

        if (request == null || (request.getLeagueId() == null && isBlank(request.getLeagueCode()))) {
            LOG.log(Level.WARNING, "League id or code is required to join a league.");
            return Optional.empty();
        }

        Optional<LeagueResponse> response = apiClient.post(JOIN_PATH, request, LeagueResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to join league.");
        }
        return response;
    }

    public Optional<List<LeagueMemberResponse>> listMembers(String leagueId){

        if (isBlank(leagueId)) {
            LOG.log(Level.WARNING, "League id is required to list members.");
            return Optional.empty();
        }

        String path = LEAGUES_PATH + "/" + leagueId + "/members";
        Optional<LeagueMemberResponse[]> response = apiClient.get(path, LeagueMemberResponse[].class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to list league members.");
        }
        return response.map(members -> new ArrayList<>(Arrays.asList(members)));
    }

    public boolean removeMember(String leagueId, String membershipId){

        if (isBlank(leagueId) || isBlank(membershipId)) {
            LOG.log(Level.WARNING, "League id and membership id are required to remove a member.");
            return false;
        }

        String path = LEAGUES_PATH + "/" + leagueId + "/members/" + membershipId;

        Optional<LeagueMemberResponse> response = apiClient.delete(path, LeagueMemberResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to remove member.");
            return false;
        }
        return true;
    }

    private boolean isValidLeagueRequest(LeagueRequest request) {
        return request != null
                && !isBlank(request.getLeagueName())
                && !isBlank(request.getDescription())
                && !isBlank(request.getLeagueType());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
