package za.ac.vzap.trytons.frontend.servlet.fixture;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.fixture.AdminFixtureRestClient;
import za.ac.vzap.trytons.frontend.client.fixture.FixtureResponse;
import za.ac.vzap.trytons.frontend.client.fixture.FixtureRestClient;
import za.ac.vzap.trytons.frontend.client.league.LeagueResponse;
import za.ac.vzap.trytons.frontend.client.league.LeagueRestClient;
import za.ac.vzap.trytons.frontend.client.results.MatchProcessingResponse;
import za.ac.vzap.trytons.frontend.client.results.MatchResultResponse;
import za.ac.vzap.trytons.frontend.client.round.RoundResponse;
import za.ac.vzap.trytons.frontend.client.round.RoundRestClient;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@WebServlet(name = "AdminFixtureServlet", urlPatterns = {"/admin/fixtures"})
public class AdminFixtureServlet extends AbstractServlet {

    private static final String VIEW = "/pages/admin-fixtures.jsp";

    @Inject
    private AdminFixtureRestClient adminFixtureRestClient;

    @Inject
    private FixtureRestClient fixtureRestClient;

    @Inject
    private RoundRestClient roundRestClient;

    @Inject
    private LeagueRestClient leagueRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAdmin(request, response)) {
            return;
        }
        loadPage(request, request.getParameter("status"));
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    /*
        Three distinct administrative writes, dispatched on the "action" parameter:

          status    (default) a purely administrative move of fixture.status
          simulate  POST /simulations/fixtures/{id}       — plays the match
          process   POST /match-processing/fixtures/{id}  — scores it

        Simulate and process are deliberately NOT status-dropdown options. COMPLETED
        and PROCESSED are derived facts, and the backend no longer lets the status
        endpoint assert them; each is now reached through the service that actually
        writes the matchResult / playerStatistics / fantasyPoints / match_team_score
        rows the status claims exist.

        All three follow POST-redirect-GET so a refresh cannot re-submit — which
        matters most for simulate, since a second run would be rejected anyway but
        noisily.
    */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAdmin(request, response)) {
            return;
        }

        String action = request.getParameter("action");
        String fixtureId = request.getParameter("fixtureId");

        if (fixtureId == null || fixtureId.isBlank()) {
            flashError(request, "A valid fixture is required.");
            redirectTo(response, request, "/admin/fixtures");
            return;
        }

        boolean handled;
        if ("simulate".equals(action)) {
            handled = simulateFixture(request, response, fixtureId.trim());
        } else if ("process".equals(action)) {
            handled = processFixture(request, response, fixtureId.trim());
        } else {
            handled = updateFixtureStatus(request, response, fixtureId.trim());
        }

        // handled == false means the call failed with a 401 and the helper has
        // already redirected to /login; the response is committed, so stop here.
        if (!handled) {
            return;
        }

        redirectTo(response, request, "/admin/fixtures");
    }

    private boolean updateFixtureStatus(HttpServletRequest request, HttpServletResponse response, String fixtureId) throws IOException {
        String status = request.getParameter("status");

        if (status == null || status.isBlank()) {
            flashError(request, "A valid status is required to update a fixture.");
            return true;
        }

        Optional<FixtureResponse> updated = adminFixtureRestClient.updateFixtureStatus(fixtureId, status.trim());
        if (updated.isPresent()) {
            flashSuccess(request, "Fixture status updated successfully.");
            return true;
        }
        return reportApiFailure(request, response, "Fixture status could not be updated.");
    }

    private boolean simulateFixture(HttpServletRequest request, HttpServletResponse response, String fixtureId) throws IOException {
        Optional<MatchResultResponse> result = adminFixtureRestClient.simulateFixture(fixtureId);
        if (result.isPresent()) {
            MatchResultResponse simulated = result.get();
            flashSuccess(request, "Fixture simulated: " + simulated.getTeamAScore() + " - " + simulated.getTeamBScore()
                    + ". Process it to award fantasy points.");
            return true;
        }
        return reportApiFailure(request, response, "The fixture could not be simulated.");
    }

    private boolean processFixture(HttpServletRequest request, HttpServletResponse response, String fixtureId) throws IOException {
        Optional<MatchProcessingResponse> result = adminFixtureRestClient.processFixture(fixtureId);
        if (result.isPresent()) {
            MatchProcessingResponse processed = result.get();
            flashSuccess(request, "Fixture processed: " + processed.getPointsCalculated()
                    + " player performances scored across " + processed.getTeamsUpdated()
                    + " teams, leaderboard refreshed.");
            return true;
        }
        return reportApiFailure(request, response, "The fixture could not be processed.");
    }

    /**
     * Turns an empty {@code Optional} into the backend's real reason.
     *
     * <p>{@code APIClient} never throws — every call comes back as an
     * {@code Optional} that is empty on a 400/403/409/500 and on a network
     * failure alike, with the reason recorded on the request-scoped
     * {@code apiCallStatus} (LESSONS.md, "APIClient never throws"). Simulation in
     * particular refuses often and for good reasons — the round has not reached
     * its lock deadline, the round is not locked, a squad is short of 20 players,
     * a current result already exists — and an administrator who is shown a
     * generic "could not simulate" has no idea which. Surface the message.
     *
     * @return {@code false} when a 401 has already been redirected and the caller
     *         must stop; {@code true} when a flash message was set and the caller
     *         should continue to its redirect.
     */
    private boolean reportApiFailure(HttpServletRequest request, HttpServletResponse response, String fallback) throws IOException {
        if (sessionExpiredRedirect(request, response)) {
            return false;
        }
        flashError(request, apiCallStatus.getMessage(fallback));
        return true;
    }

    /*
        Mirrors FixtureServiceImpl.isValidStatusTransition on the backend so the
        per-row dropdown only offers moves the server will accept. Keep the two in
        sync.

        COMPLETED and PROCESSED are absent by design — they are not administrative
        moves and the backend rejects them here. They are reached with the
        "Simulate" and "Process" buttons instead. LOCKED -> SIMULATING is gone too:
        simulation only accepts a LOCKED fixture, so it was a dead end.
    */
    private Map<String, List<String>> statusTransitions() {
        Map<String, List<String>> transitions = new LinkedHashMap<>();
        transitions.put("UPCOMING", List.of("LOCKED", "CANCELLED"));
        transitions.put("LOCKED", List.of("CANCELLED"));
        transitions.put("SIMULATING", List.of("CANCELLED"));
        transitions.put("COMPLETED", List.of());
        transitions.put("PROCESSED", List.of());
        transitions.put("CANCELLED", List.of());
        return transitions;
    }


    private void loadPage(HttpServletRequest request, String statusFilter) {
        Optional<List<FixtureResponse>> fixtures = fixtureRestClient.listFixtures(statusFilter);
        if (fixtures.isPresent()) {
            request.setAttribute("fixtures", fixtures.get());
        } else {
            request.setAttribute("fixturesError", "Unable to load fixtures");
            request.setAttribute("fixtures", List.of());
        }
        request.setAttribute("statusFilter", statusFilter);

        // Valid next-status choices per current status, so the per-row dropdown only
        // offers transitions the backend will accept (see statusTransitions()).
        request.setAttribute("statusTransitions", statusTransitions());

        // Fetched only to label the table: the page no longer offers league/round
        // pickers now that fixtures are generated with the tournament.
        List<RoundResponse> rounds = roundRestClient.listRounds().orElse(List.of());
        List<LeagueResponse> leagueList = leagueRestClient.listPublicLeagues().orElse(List.of());

        // Name lookups so the fixtures table can show a league name and round label
        // instead of raw ids. Keyed by the id's string form; the JSP looks them up
        // with ${map[fixture.leagueId.toString()]} since the fixture carries UUIDs.
        Map<String, String> leagueNames = new HashMap<>();
        leagueList.forEach(l -> leagueNames.put(l.getLeagueId(), l.getLeagueName()));
        request.setAttribute("leagueNamesById", leagueNames);

        Map<String, String> roundLabels = new HashMap<>();
        rounds.forEach(r ->
                roundLabels.put(r.getRoundId(), r.getSeason() + " · Round " + r.getRoundNumber()));
        request.setAttribute("roundLabelsById", roundLabels);
    }

    @Override
    public String getServletInfo() {
        return "Admin Fixture Servlet, handles fixture listing and fixture status updates";
    }
}
