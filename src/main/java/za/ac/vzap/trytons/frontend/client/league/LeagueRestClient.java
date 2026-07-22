package za.ac.vzap.trytons.frontend.client.league;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import za.ac.vzap.trytons.frontend.client.shared.APIClient;

@Dependent
public class LeagueRestClient {

    private static final String LEAGUES_PATH = "/league";
    // Backend LeagueResource.getAllLeagues() declares no @QueryParam - "mine" was always ignored and
    // this returned the exact same public+member list as listPublicLeagues(). Kept as its own method
    // (rather than collapsed into listPublicLeagues) so callers can filter client-side by managerUserId;
    // see LeagueServlet, which is the only place that can actually narrow this to "my leagues".
    private static final String MY_LEAGUES_PATH = "/league";
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

    public Optional<JoinLeagueResponse> joinLeague(JoinLeagueRequest request){

        if (request == null || (request.getLeagueId() == null && isBlank(request.getLeagueCode()))) {
            LOG.log(Level.WARNING, "League id or code is required to join a league.");
            return Optional.empty();
        }

        // Backend POST /league/join returns a JoinLeagueResponseDTO (leagueId, leagueName, message,
        // membershipId), not a full LeagueResponseDTO - deserializing into LeagueResponse here used to
        // silently null out every field the backend never sends (description, leagueType, etc.).
        Optional<JoinLeagueResponse> response = apiClient.post(JOIN_PATH, request, JoinLeagueResponse.class);
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

    // Backend returns 204 No Content on a successful removal, which APIClient.handle() always maps to
    // Optional.empty() - so Optional emptiness can never distinguish success from failure here. Return
    // Optional<Void> and let the caller check the request-scoped ApiCallStatus.isSuccess() instead.
    public Optional<Void> removeMember(String leagueId, String membershipId){

        if (isBlank(leagueId) || isBlank(membershipId)) {
            LOG.log(Level.WARNING, "League id and membership id are required to remove a member.");
            return Optional.empty();
        }

        String path = LEAGUES_PATH + "/" + leagueId + "/members/" + membershipId;
        return apiClient.delete(path, Void.class);
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
