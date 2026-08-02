package za.ac.vzap.trytons.frontend.client.tournament;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import za.ac.vzap.trytons.frontend.client.shared.APIClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Talks to the backend /tournaments resource. Every method returns an Optional
 * that is empty on any non-2xx or network failure — the caller inspects
 * ApiCallStatus for the reason, per the APIClient contract.
 */
@Dependent
public class TournamentRestClient {

    private static final String TOURNAMENTS_PATH = "/tournaments";
    private static final Logger LOG = Logger.getLogger(TournamentRestClient.class.getName());

    @Inject
    private APIClient apiClient;

    /** POST /tournaments/leagues/{leagueId}/start — league manager or admin only. */
    public Optional<StartLeagueResponse> startLeague(String leagueId) {
        if (isBlank(leagueId)) {
            LOG.log(Level.WARNING, "League id is required to start a tournament.");
            return Optional.empty();
        }
        String path = TOURNAMENTS_PATH + "/leagues/" + encode(leagueId) + "/start";
        // The endpoint carries everything in the path and takes no body; an empty
        // JSON object is sent rather than a null entity, which JAX-RS rejects.
        Optional<StartLeagueResponse> response = apiClient.post(path, Map.of(), StartLeagueResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to start the league tournament.");
        }
        return response;
    }

    /** GET /tournaments/leagues/{leagueId} — 404 when the league has never been started. */
    public Optional<TournamentResponse> getTournamentForLeague(String leagueId) {
        if (isBlank(leagueId)) {
            LOG.log(Level.WARNING, "League id is required to load a tournament.");
            return Optional.empty();
        }
        String path = TOURNAMENTS_PATH + "/leagues/" + encode(leagueId);
        Optional<TournamentResponse> response = apiClient.get(path, TournamentResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to load the tournament for the league.");
        }
        return response;
    }

    /** GET /tournaments/{tournamentId} */
    public Optional<TournamentResponse> getTournament(String tournamentId) {
        if (isBlank(tournamentId)) {
            LOG.log(Level.WARNING, "Tournament id is required to load a tournament.");
            return Optional.empty();
        }
        String path = TOURNAMENTS_PATH + "/" + encode(tournamentId);
        Optional<TournamentResponse> response = apiClient.get(path, TournamentResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to load the tournament.");
        }
        return response;
    }

    /** GET /tournaments/{tournamentId}/fixtures — pool and knockout fixtures together. */
    public Optional<List<TournamentFixtureResponse>> listFixtures(String tournamentId) {
        if (isBlank(tournamentId)) {
            LOG.log(Level.WARNING, "Tournament id is required to list tournament fixtures.");
            return Optional.empty();
        }
        String path = TOURNAMENTS_PATH + "/" + encode(tournamentId) + "/fixtures";
        Optional<TournamentFixtureResponse[]> response =
                apiClient.get(path, TournamentFixtureResponse[].class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to list tournament fixtures.");
        }
        return response.map(fixtures -> new ArrayList<>(Arrays.asList(fixtures)));
    }

    /**
     * PUT /tournaments/leagues/{leagueId}/rounds/{roundId}/match-day — moves a
     * round and every fixture in it to another match day.
     *
     * <p>Not an admin-only call: the backend allows a PRIVATE league's own
     * manager to move their own match days, and only withholds a PUBLIC
     * league's calendar. A 403 comes back as an empty Optional with the reason
     * on ApiCallStatus, per the APIClient contract.
     */
    public Optional<MatchDayResponse> updateMatchDay(String leagueId, String roundId, LocalDate matchDay) {
        if (isBlank(leagueId) || isBlank(roundId) || matchDay == null) {
            LOG.log(Level.WARNING, "A league, a round and a match day are required to reschedule a round.");
            return Optional.empty();
        }
        String path = TOURNAMENTS_PATH + "/leagues/" + encode(leagueId)
                + "/rounds/" + encode(roundId) + "/match-day";
        Optional<MatchDayResponse> response =
                apiClient.put(path, new MatchDayUpdateRequest(matchDay), MatchDayResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to reschedule the round''s match day.");
        }
        return response;
    }

    /** GET /tournaments/settings */
    public Optional<TournamentSettingsResponse> getSettings() {
        Optional<TournamentSettingsResponse> response =
                apiClient.get(TOURNAMENTS_PATH + "/settings", TournamentSettingsResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to load tournament settings.");
        }
        return response;
    }

    /** PUT /tournaments/settings — admin only. */
    public Optional<TournamentSettingsResponse> updateSettings(TournamentSettingsResponse settings) {
        if (settings == null) {
            LOG.log(Level.WARNING, "Settings are required to update tournament settings.");
            return Optional.empty();
        }
        Optional<TournamentSettingsResponse> response =
                apiClient.put(TOURNAMENTS_PATH + "/settings", settings, TournamentSettingsResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to update tournament settings.");
        }
        return response;
    }

    /** POST /tournaments/{tournamentId}/advance — admin only. */
    public Optional<TournamentResponse> advance(String tournamentId) {
        if (isBlank(tournamentId)) {
            LOG.log(Level.WARNING, "Tournament id is required to advance a tournament.");
            return Optional.empty();
        }
        String path = TOURNAMENTS_PATH + "/" + encode(tournamentId) + "/advance";
        Optional<TournamentResponse> response = apiClient.post(path, Map.of(), TournamentResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to advance the tournament.");
        }
        return response;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
